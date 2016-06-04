package br.com.smartpush.f;


import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import br.com.smartpush.R;
import br.com.smartpush.Smartpush;
import br.com.smartpush.SmartpushActivity;
import br.com.smartpush.SmartpushListenerService;
import br.com.smartpush.u.SmartpushConnectivityUtil;
import br.com.smartpush.u.SmartpushHitUtils;
import br.com.smartpush.u.SmartpushHttpClient;
import br.com.smartpush.u.SmartpushLog;
import br.com.smartpush.u.SmartpushUtils;

import static br.com.smartpush.u.SmartpushUtils.TAG;

/**
 * Created by fabio.licks on 20/08/15.
 */
public class VideoPlayerFragment extends Fragment implements
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
        SmartpushLog.getInstance( getActivity() ).d( TAG, "onCreate" );
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
        SmartpushLog.getInstance( getActivity() ).d( TAG, "onCreateView" );
        return inflater.inflate( R.layout.player, null );
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        SmartpushLog.getInstance( getActivity() ).d( TAG, "onViewCreated" );
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
        SmartpushLog.getInstance( getActivity() ).d( TAG, "onStart" );
        super.onStart();

        if ( mediaPlayer != null ) {
            handler.post( showSurface );
            handler.postDelayed( updateSeekBarTime, 10 );

            if ( isControlsVisible ) {
                handler.post( showHideControlsTask );
                isControlsVisible = !isControlsVisible;
            }
        } else {
            new FetchVideoDeepLink()
                    .execute(
                            getActivity().getIntent().getStringExtra(
                                    SmartpushListenerService.VIDEO_URI ) );
        }
    }

    @Override
    public void onStop() {
        SmartpushLog.getInstance( getActivity() ).d( TAG, "onStop" );
        super.onStop();
        if( getActivity().isChangingConfigurations() ) {
            SmartpushLog.getInstance( getActivity() ).d( TAG, "configuration is changing: keep playing" );
        } else {
            destroyMediaPlayer();
        }
        handler.removeCallbacks( updateSeekBarTime );
    }

    @Override
    public void onCompletion( MediaPlayer mp ) {
        SmartpushLog.getInstance( getActivity() ).d( TAG, "onCompletion" );

        // Tracking
        hit( STATE.FINISHED.name() );
        //

        goForward();
    }

    @Override
    public void onPrepared( MediaPlayer mp ) {
        SmartpushLog.getInstance( getActivity() ).d( TAG, "onPrepared" );
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
        SmartpushLog.getInstance( getActivity() ).d( TAG, "onError" );
        return false;
    }

    @Override
    public void surfaceCreated( SurfaceHolder holder ) {
        SmartpushLog.getInstance( getActivity() ).d( TAG, "surfaceCreated" );

        mediaPlayer.setDisplay( holder );
        handler.post( resizeSurfaceTask );
    }

    @Override
    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {

    }

    @Override
    public void surfaceDestroyed( SurfaceHolder holder) {
        SmartpushLog.getInstance( getActivity() ).d(TAG, "surfaceDestroyed");
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
        SmartpushLog.getInstance( getActivity() ).d(TAG, "setSurfaceSize");
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

        String url = getArguments().getString( SmartpushListenerService.URL );

        if ( url != null ) {
            if ( url.startsWith( "http" ) || url.startsWith( "https" ) ) {
                Intent it = new Intent( getActivity(), SmartpushActivity.class );
                it.putExtra( SmartpushListenerService.URL, url );
                it.putExtra( SmartpushUtils.ONLY_PORTRAIT, true );
                it.putExtra( SmartpushUtils.REDIRECTED, true );
                it.putExtra( SmartpushHitUtils.Fields.PUSH_ID.getParamName(),
                        getArguments().getString( SmartpushHitUtils.Fields.PUSH_ID.getParamName() ) );
                it.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
                startActivity(it);
            } else if ( url.startsWith( "market://details?id=" ) ) {
                Intent it = new Intent( Intent.ACTION_VIEW );
                it.setData(Uri.parse(url));
                if ( it.resolveActivity(getActivity().getPackageManager()) != null ) {
                    startActivity(it);
                }
                getActivity().finish();
            } else {
                Intent intent = new Intent();
                intent.setData( Uri.parse( url ) );
                if ( intent.resolveActivity(getActivity().getPackageManager() ) != null ) {
                    startActivity(intent);
                }
                getActivity().finish();
            }
        }
    }

    private void createMediaPlayer( Uri video ) {
        SmartpushLog.getInstance( getActivity() ).d(TAG, "createMediaPlayer");
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setDataSource(getActivity(), video);
            //player will be started after completion of preparing...
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            SmartpushLog.getInstance( getActivity() ).e( TAG, e.getMessage(), e );
        }
    }

    private void destroyMediaPlayer() {
        SmartpushLog.getInstance( getActivity() ).d( TAG, "destroyMediaPlayer" );

        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
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

    private class FetchVideoDeepLink extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground( String... params ) {
            String resp  = SmartpushHttpClient.get( "play/" + params[0] + "/info", null, getActivity() );

            try {
                JSONObject json = new JSONObject( resp );
                int code = json.has( "code" ) ? json.getInt( "code" ) : 0;

                if ( code == 200 ) {
                    JSONArray videos = json.getJSONArray( "videos" );
                    for ( int i = 0; i < videos.length(); i++ ) {
                        JSONObject o = videos.getJSONObject( i );
                        if ( SmartpushConnectivityUtil.isConnectedWifi( getActivity() )
                            || ( SmartpushConnectivityUtil.isConnectedMobile( getActivity() )
                                && SmartpushConnectivityUtil.isConnectedFast( getActivity() ) ) ) {

                            if ( o.getString( "resolution" ).equals( "640x360" )
                                    && o.getString( "extension" ).equals( "mp4" ) ) {
                                return o.getString( "url" );
                            }

                        } else {
                            if ( o.getString( "resolution" ).equals( "320x240" )
                                    && o.getString( "extension" ).equals( "3gp" ) ) {
                                return o.getString( "url" );
                            }
                        }
                    }
                }

            } catch ( JSONException e ) {
                SmartpushLog.getInstance( getActivity() ).e( TAG, e.getMessage(), e );
            }

            return null;
        }

        @Override
        protected void onPostExecute( String deepLink ) {
            if ( deepLink != null ) {
                createMediaPlayer(Uri.parse(deepLink));
            } else {
                goForward();
            }
        }
    }

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
}