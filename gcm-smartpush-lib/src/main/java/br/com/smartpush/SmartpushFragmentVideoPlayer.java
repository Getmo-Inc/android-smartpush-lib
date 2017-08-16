package br.com.smartpush;


import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import static br.com.smartpush.Utils.Constants.NOTIF_PLAY_VIDEO_ONLY_WIFI;
import static br.com.smartpush.Utils.Constants.NOTIF_URL;
import static br.com.smartpush.Utils.Constants.NOTIF_PACKAGENAME;


/**
 * Created by fabio.licks on 20/08/15.
 */
public final class SmartpushFragmentVideoPlayer extends Fragment implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener, SurfaceHolder.Callback, View.OnClickListener {

    private enum STATE {
      PREPARING, PLAY, Q1, Q2, Q3, FINISHED, JUMP, CANCEL
    };

    private STATE myCurrentState = STATE.PREPARING;

    private MediaPlayer   mediaPlayer;
    private SurfaceView   surfaceView;
    private SurfaceHolder surfaceHolder;
    private ProgressBar   loading;
    private ProgressBar   progressbar;
    private TextView      duration;

    private Button        mBtnClose;
    private LinearLayout  mControls;

    private Animation     fadeOut, fadeIn;

    Handler               handler = new Handler();
    private double        timeElapsed = 0, finalTime = 0;

    private static boolean isControlsVisible = false;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        SmartpushLog.d( Utils.TAG, "onCreate" );
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        //
        fadeOut = new AlphaAnimation( 1, 0 );
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(500);

        //
        fadeIn = new AlphaAnimation( 0, 1 );
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(250);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        SmartpushLog.d( Utils.TAG, "onCreateView" );
        return inflater.inflate( R.layout.player, null );
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        SmartpushLog.d( Utils.TAG, "onViewCreated" );
        loading       = ( ProgressBar )view.findViewById( R.id.loading );
        surfaceView   = ( SurfaceView ) view.findViewById( R.id.surface );
        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.addCallback( this );
        surfaceView.setOnClickListener( this );

        // Controls
        mControls = ( LinearLayout ) view.findViewById( R.id.controls );
        mBtnClose = ( Button ) view.findViewById( R.id.btnClose );
        mBtnClose.setOnClickListener( this );
        duration  = ( TextView ) view.findViewById( R.id.duration );

        progressbar = ( ProgressBar ) view.findViewById( R.id.progressbar );
    }

    @Override
    public void onStart() {
        SmartpushLog.d( Utils.TAG, "onStart" );
        super.onStart();

        if ( isSetToPlayOnlyWifi() && !SmartpushConnectivityUtil.isConnectedWifi( getActivity() ) ) {
                goForward();
        } else {
            if ( mediaPlayer != null ) {
                handler.post( showSurface );
                handler.postDelayed( updateSeekBarTime, 10 );

                if ( isControlsVisible ) {
                    handler.post( showHideControlsTask );
                    isControlsVisible = !isControlsVisible;
                }
            } else {
// TODO trabalhando aqui !!!!! <------------------------------------
//                createMediaPlayer(Uri.parse(deepLink));
//                new FetchVideoDeepLink()
//                        .execute(
//                                getActivity().getIntent().getStringExtra(
//                                        SmartpushListenerService.NOTIF_VIDEO_URI ) );
            }
        }
    }

    @Override
    public void onStop() {
        SmartpushLog.d( Utils.TAG, "onStop" );
        super.onStop();
        if( getActivity().isChangingConfigurations() ) {
            SmartpushLog.d( Utils.TAG, "configuration is changing: keep playing" );
        } else {
            destroyMediaPlayer();
        }
        handler.removeCallbacks( updateSeekBarTime );
    }

    @Override
    public void onCompletion( MediaPlayer mp ) {
        SmartpushLog.d( Utils.TAG, "onCompletion" );

        // Tracking
        hit( STATE.FINISHED.name() );
        //

        goForward();
    }

    @Override
    public void onPrepared( MediaPlayer mp ) {
        SmartpushLog.d( Utils.TAG, "onPrepared" );
//        handler.post( resizeSurfaceTask );
        handler.post( showSurface );
        finalTime = mediaPlayer.getDuration();
        mediaPlayer.start();
        handler.postDelayed(updateSeekBarTime, 10);

        // Tracking
        hit( STATE.PLAY.name() );
        //
    }

    @Override
    public boolean onError( MediaPlayer mp, int what, int extra ) {
        SmartpushLog.d( Utils.TAG, "onError" );
        return false;
    }

    @Override
    public void surfaceCreated( SurfaceHolder holder ) {
        SmartpushLog.d( Utils.TAG, "surfaceCreated" );

        mediaPlayer.setDisplay( holder );
        handler.post( resizeSurfaceTask );
    }

    @Override
    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
        SmartpushLog.d( Utils.TAG, "surfaceChanged" );

    }

    @Override
    public void surfaceDestroyed( SurfaceHolder holder) {
        SmartpushLog.d( Utils.TAG, "surfaceDestroyed");
        if ( mediaPlayer != null )
            mediaPlayer.setDisplay( null );
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if ( percent <= 100 ) {
            progressbar.setSecondaryProgress( percent );
        }
    }

    private void setSurfaceSize() {
        SmartpushLog.d( Utils.TAG, "setSurfaceSize");
        // get the dimensions of the video (only valid when surfaceView is set)
        float videoWidth = mediaPlayer.getVideoWidth();
        float videoHeight = mediaPlayer.getVideoHeight();

        // get the dimensions of the container (the surfaceView's parent in this case)
        View container = (View) surfaceView.getParent();
        float containerWidth = container.getWidth();
        float containerHeight = container.getHeight();

        // set dimensions to surfaceView's layout params (maintaining aspect ratio)
        android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        lp.width = (int) containerWidth;
        lp.height = (int) ((videoHeight / videoWidth) * containerWidth);
        if(lp.height > containerHeight) {
            lp.width = (int) ((videoWidth / videoHeight) * containerHeight);
            lp.height = (int) containerHeight;
        }

        surfaceView.setLayoutParams( lp );
    }

    private void goForward() {
        String url         = getArguments().getString( NOTIF_URL );
        String packageName = getArguments().getString( NOTIF_PACKAGENAME );

        if ( packageName != null ) {
            Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage( packageName );
            intent.putExtras( getArguments() );
            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
            getActivity().finish();
        } else if ( url != null ) {
            if ( url.startsWith( "http" ) || url.startsWith( "https" ) ) {
                Intent it = new Intent( getActivity(), SmartpushActivity.class );
                it.putExtra( NOTIF_URL, url );
                it.putExtra( Utils.Constants.ONLY_PORTRAIT, true );
                it.putExtra( Utils.Constants.REDIRECTED, true );
                it.putExtra( SmartpushHitUtils.Fields.PUSH_ID.getParamName(),
                        getArguments().getString( SmartpushHitUtils.Fields.PUSH_ID.getParamName() ) );
                it.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
                startActivity(it);
            } else if ( url.startsWith( "market://details?id=" ) ) {
                Intent it = new Intent( Intent.ACTION_VIEW );
                it.setData(Uri.parse(url));
                it.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                if ( it.resolveActivity(getActivity().getPackageManager()) != null ) {
                    startActivity(it);
                }
                getActivity().finish();
            } else {
                Intent intent = new Intent();
                intent.putExtras( getArguments() );
                intent.setData( Uri.parse( url ) );
                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                if ( intent.resolveActivity(getActivity().getPackageManager() ) != null ) {
                    startActivity(intent);
                }
                getActivity().finish();
            }
        }
    }

    private void createMediaPlayer( Uri video ) {
        SmartpushLog.d( Utils.TAG, "createMediaPlayer");
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//
//            mediaPlayer.setAu
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setDataSource( getActivity(), video );
            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            SmartpushLog.e( Utils.TAG, e.getMessage(), e );
        }
    }

    private void destroyMediaPlayer() {
        SmartpushLog.d( Utils.TAG, "destroyMediaPlayer" );
        if ( mediaPlayer != null ) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onClick( View view ) {
        if ( view.getId() == R.id.surface ) {
            handler.post( showHideControlsTask );
        } else if ( view.getId() == R.id.btnClose ) {
            // Tracking
            hit( STATE.JUMP.name() );
            //
            goForward();
        }
    }

    private Runnable showHideControlsTask = new Runnable() {
        @Override
        public void run() {
            Animation    a = ( isControlsVisible ) ? fadeOut   : fadeIn;
            int visibility = ( isControlsVisible ) ? View.GONE : View.VISIBLE;

            mControls.startAnimation( a );
            mControls.setVisibility( visibility );
            mBtnClose.startAnimation( a );
            mBtnClose.setVisibility( visibility );

            isControlsVisible = !isControlsVisible;
        }
    };

    private Runnable resizeSurfaceTask = new Runnable() {
        @Override
        public void run() {
            setSurfaceSize();
        }
    };

    private Runnable updateSeekBarTime = new Runnable() {
        @Override
        public void run() {
            // current position
            timeElapsed = mediaPlayer.getCurrentPosition();
            // set seekbar progress
            progressbar.setProgress( ( int )( ( timeElapsed / finalTime ) * 100 )  );
            double timeRemaining = finalTime - timeElapsed;
            // set time remaining
            duration.setText( String.format( "%02d:%02d", TimeUnit.MILLISECONDS.toMinutes( ( long ) timeRemaining ),
                    TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));
            // repeat yourself that again in 100 miliseconds
            handler.postDelayed( this, 100 );

            // tracking
            double percentPlayed = timeElapsed / finalTime;
            if ( percentPlayed >= 0.25 && percentPlayed < 0.5  ) {
                if ( myCurrentState != STATE.Q1 ) {
                    myCurrentState = STATE.Q1;
                    // Tracking
                    hit( STATE.Q1.name() );
                    //
                }
            } else if ( percentPlayed >= 0.5 && percentPlayed < 0.75  ) {
                if ( myCurrentState != STATE.Q2 ) {
                    myCurrentState = STATE.Q2;
                    // Tracking
                    hit( STATE.Q2.name() );
                    //
                }
            } else if ( percentPlayed >= 0.75  ) {
                if ( myCurrentState != STATE.Q3 ) {
                    myCurrentState = STATE.Q3;
                    // Tracking
                    hit( STATE.Q3.name() );
                    //
                }
            }
        }
    };

    private Runnable showSurface = new Runnable() {
        @Override
        public void run() {
            loading.setVisibility( View.GONE );
            surfaceView.startAnimation( fadeIn );
            surfaceView.setVisibility( View.VISIBLE );
        }
    };

//    private class FetchVideoDeepLink extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground( String... params ) {
//            String resp  = SmartpushHttpClient.get( "play/" + params[0] + "/info", null, getActivity() );
//
//            try {
//                JSONObject json = new JSONObject( resp );
//                boolean status = json.has( "status" ) ? json.getBoolean( "status" ) : false;
//
//                if ( status ) {
//                    JSONArray videos = json.getJSONArray( "videos" );
//
//                    int idSelected = -1;
//                    String extension = ( SmartpushConnectivityUtil.isConnectedWifi( getActivity() ) ) ? "mp4" : "3gp";
//
//                    int bestSize = ( "mp4".equals( extension ) ) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
//
//                    for ( int i = 0; i < videos.length(); i++ ) {
//                        JSONObject o = videos.getJSONObject( i );
//
//                        if ( o.getString( "extension" ).equals( extension ) ) {
//                            int currentSize = o.getInt( "filesize" );
//
//                            if ( "3gp".equals( extension ) ) {
//                                if (bestSize < currentSize) {
//                                    bestSize = currentSize;
//                                    idSelected = i;
//                                }
//                            } else {
//                                if (bestSize > currentSize) {
//                                    bestSize = currentSize;
//                                    idSelected = i;
//                                }
//                            }
//                        }
//                    }
//
//                    String linkVideo = ( idSelected != -1 ) ? videos.getJSONObject( idSelected ).getString( "url" ) : null;
//                    SmartpushLog.d( Utils.TAG, "---> " + linkVideo );
//
//                    return linkVideo;
//                }
//
//            } catch ( Exception e ) {
//                SmartpushLog.e( Utils.TAG, e.getMessage(), e );
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute( String deepLink ) {
//            if ( deepLink != null ) {
//                createMediaPlayer(Uri.parse(deepLink));
//            } else {
//                goForward();
//            }
//        }
//    }

    private void hit ( String action ) {
        // Tracking
        String pushId =
                SmartpushHitUtils.getValueFromPayload(
                        SmartpushHitUtils.Fields.PUSH_ID, getActivity().getIntent().getExtras() );

        if ( !"".equals( pushId ) ) {
            Smartpush.hit( getActivity(), pushId, "PLAYER", null, action, null );
        }
        //
    }

    private boolean isSetToPlayOnlyWifi() {
        boolean playVideoOnlyWifi = false;
        Bundle extras = getArguments();
        if ( extras != null && extras.containsKey( NOTIF_PLAY_VIDEO_ONLY_WIFI) ) {
            playVideoOnlyWifi =
                    ( extras.getString( NOTIF_PLAY_VIDEO_ONLY_WIFI ).equals( "1" ) ) ? true : false;
        }

        return playVideoOnlyWifi;
    }
}