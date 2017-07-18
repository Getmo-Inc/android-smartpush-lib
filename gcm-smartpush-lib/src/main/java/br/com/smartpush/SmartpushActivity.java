package br.com.smartpush;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;


public final class SmartpushActivity extends AppCompatActivity {

    Fragment current;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        // Set default theme
        setTheme( R.style.Theme_AppCompat_Light_NoActionBar );

        super.onCreate( savedInstanceState );

        // (un)lock screen orientation!
        if ( !getIntent().hasExtra( Utils.Constants.ONLY_PORTRAIT ) ) {
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED );
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.container);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if ( findViewById( R.id.fragment_container ) != null ) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if ( savedInstanceState != null ) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            current =
                    getIntent().hasExtra( SmartpushListenerService.VIDEO_URI )
                            ? new SmartpushFragmentVideoPlayer() : new SmartpushFragmentWebView();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            current.setArguments( getIntent().getExtras() );

            // [START] Tracking
            Bundle extras = getIntent().getExtras();
            String pushId = SmartpushHitUtils
                    .getValueFromPayload( SmartpushHitUtils.Fields.PUSH_ID, extras );

            if ( extras != null && !"".equals( pushId ) ) {

                SmartpushHitUtils.Action action =
                        ( extras.containsKey( Utils.Constants.REDIRECTED ) )
                                ? SmartpushHitUtils.Action.REDIRECTED : SmartpushHitUtils.Action.CLICKED;

                Smartpush.hit(this, pushId, null, null, action, null);
            }
            // [END] Tracking

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add( R.id.fragment_container, current ).commit();
        }
    }

    @Override
    protected void onNewIntent( Intent intent ) {
        super.onNewIntent( intent );

        // update intent!
        setIntent( intent );

        // (un)lock screen orientation!
        if ( !intent.hasExtra( Utils.Constants.ONLY_PORTRAIT ) ) {
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED );
        } else {
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        }

        // Create a new Fragment to be placed in the activity layout
        current =
                intent.hasExtra( SmartpushListenerService.VIDEO_URI )
                        ? new SmartpushFragmentVideoPlayer() : new SmartpushFragmentWebView();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        current.setArguments( intent.getExtras() );

        // [START] Tracking
        Bundle extras = getIntent().getExtras();
        String pushId = SmartpushHitUtils
                .getValueFromPayload( SmartpushHitUtils.Fields.PUSH_ID, extras );

        if ( extras != null && !"".equals( pushId ) ) {
            SmartpushHitUtils.Action action =
                    ( extras.containsKey( Utils.Constants.REDIRECTED ) )
                            ? SmartpushHitUtils.Action.REDIRECTED : SmartpushHitUtils.Action.CLICKED;

            Smartpush.hit( this, pushId, null, null, action, null );
        }
        // [END] Tracking

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .replace( R.id.fragment_container, current ).commit();
    }

    @Override
    public void onBackPressed() {

        if ( current instanceof SmartpushFragmentVideoPlayer) {
            // Tracking
            String pushId =
                    SmartpushHitUtils.getValueFromPayload(
                            SmartpushHitUtils.Fields.PUSH_ID, getIntent().getExtras());
            if ( !"".equals( pushId ) ) {
                Smartpush.hit( this, pushId, "PLAYER", null, "CANCEL", null );
            }
            //
        }

        super.onBackPressed();
    }
}
