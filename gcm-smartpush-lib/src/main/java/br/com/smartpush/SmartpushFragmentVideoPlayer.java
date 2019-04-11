package br.com.smartpush;


import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import java.io.File;
import java.util.concurrent.TimeUnit;

import static br.com.smartpush.Utils.Constants.NOTIF_PACKAGENAME;
import static br.com.smartpush.Utils.Constants.NOTIF_URL;
import static br.com.smartpush.Utils.Constants.NOTIF_VIDEO_URI;
import static br.com.smartpush.Utils.Constants.OPEN_IN_BROWSER;
import static br.com.smartpush.Utils.TAG;


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

    private FetchVideoDeepLink fetchVideoDeepLinkTask;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        SmartpushLog.d( TAG, "onCreate" );
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
        SmartpushLog.d( TAG, "onCreateView" );
        return inflater.inflate( R.layout.player, null );
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        SmartpushLog.d( TAG, "onViewCreated" );
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
        SmartpushLog.d( TAG, "onStart" );
        super.onStart();

        if ( mediaPlayer != null ) {
            handler.post( showSurface );
            handler.postDelayed( updateSeekBarTime, 10 );

            if ( isControlsVisible ) {
                handler.post( showHideControlsTask );
                isControlsVisible = !isControlsVisible;
            }
        } else {
            fetchVideoDeepLinkTask = new FetchVideoDeepLink();
            fetchVideoDeepLinkTask.execute( getActivity().getIntent().getStringExtra( NOTIF_VIDEO_URI ) );
        }
    }

    @Override
    public void onStop() {
        SmartpushLog.d( TAG, "onStop" );
        super.onStop();
        if( getActivity().isChangingConfigurations() ) {
            SmartpushLog.d( TAG, "configuration is changing: keep playing" );
        } else {
            if ( fetchVideoDeepLinkTask != null
                    && fetchVideoDeepLinkTask.getStatus() == AsyncTask.Status.RUNNING ) {
                fetchVideoDeepLinkTask.cancel( true );
            }
            destroyMediaPlayer();
        }
        handler.removeCallbacks( updateSeekBarTime );
    }

    @Override
    public void onCompletion( MediaPlayer mp ) {
        SmartpushLog.d( TAG, "onCompletion" );

        // Tracking
        hit( STATE.FINISHED.name() );
        //

        redirectToContent();
    }

    @Override
    public void onPrepared( MediaPlayer mp ) {
        SmartpushLog.d( TAG, "onPrepared" );
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
        SmartpushLog.d( TAG, "onError" );
        return false;
    }

    @Override
    public void surfaceCreated( SurfaceHolder holder ) {
        SmartpushLog.d( TAG, "surfaceCreated" );

        mediaPlayer.setDisplay( holder );
        handler.post( resizeSurfaceTask );
    }

    @Override
    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
        SmartpushLog.d( TAG, "surfaceChanged" );

    }

    @Override
    public void surfaceDestroyed( SurfaceHolder holder) {
        SmartpushLog.d( TAG, "surfaceDestroyed");
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
        SmartpushLog.d( TAG, "setSurfaceSize");
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

    private void redirectToContent() {
        String url         = getArguments().getString( NOTIF_URL );
        String packageName = getArguments().getString( NOTIF_PACKAGENAME );

        Bundle extras = getArguments();
        if ( extras != null ) {
            // remove o link do video para permitir logica unica de selecao da Intent de destino.
            extras.remove( NOTIF_VIDEO_URI );

            // informa que a navegacao eh um redirecionamento.
            extras.putBoolean( Utils.Constants.REDIRECTED, true );
        }

        Intent intent =
                Utils.Smartpush.getIntentToRedirect( getActivity(), url, packageName, extras );

        if ( intent != null ) {
            if ( intent.resolveActivity( getActivity().getPackageManager() ) != null ) {
                getActivity().startActivity( intent );
            }

            if ( extras.getInt( OPEN_IN_BROWSER, 1 ) != 0 ) {
                getActivity().finish();
            }
        }
    }

    private void createMediaPlayer( File video ) {
        SmartpushLog.d( TAG, "createMediaPlayer");
        try {
            mediaPlayer = MediaPlayer.create( getActivity(), Uri.parse( video.getAbsolutePath() ) );
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            SmartpushLog.e( TAG, e.getMessage(), e );
        }
    }

    private void destroyMediaPlayer() {
        SmartpushLog.d( TAG, "destroyMediaPlayer" );
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
            redirectToContent();
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

    private class FetchVideoDeepLink extends AsyncTask<String, Void, File> {
        @Override
        protected File doInBackground(String... params) {
            Log.d( Utils.TAG, "LINK: " + params[0] );

            String key =
                    CacheManager
                            .getInstance( getActivity() )
                            .prefetchVideo( params[0], CacheManager.ExpirationTime.NONE );

            Log.d( Utils.TAG, "KEY: " + key );

            return CacheManager
                    .getInstance( getActivity() )
                    .getFile( key );
        }

        @Override
        protected void onPostExecute( File videoInCache ) {
            if ( videoInCache != null ) {
                Log.d( Utils.TAG, "PATH: " + videoInCache.getAbsolutePath() );
                createMediaPlayer( videoInCache );
            } else {
                redirectToContent();
            }
        }
    }

    private void hit ( String action ) {
        Activity root = getActivity();
        if ( root != null ) {
            // Tracking
            String pushId =
                    SmartpushHitUtils.getValueFromPayload(
                            SmartpushHitUtils.Fields.PUSH_ID, root.getIntent().getExtras());

            String alias = SmartpushHitUtils
                    .getValueFromPayload( SmartpushHitUtils.Fields.ALIAS, root.getIntent().getExtras() );

            if (!"".equals(pushId)) {
                Smartpush.hit( root, alias, pushId, "PLAYER", null, action, null);
            }
        }
        //
    }
}