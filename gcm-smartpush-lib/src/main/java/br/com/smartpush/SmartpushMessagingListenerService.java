package br.com.smartpush;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.util.Strings;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import static br.com.smartpush.Utils.Constants.LAUNCH_ICON;
import static br.com.smartpush.Utils.Constants.NOTIF_TITLE;
import static br.com.smartpush.Utils.Constants.NOTIF_URL;
import static br.com.smartpush.Utils.Constants.NOTIF_VIDEO_URI;
import static br.com.smartpush.Utils.TAG;

/**
 * Created by fabio.licks on 10/02/16.
 */
public abstract class SmartpushMessagingListenerService extends FirebaseMessagingService {

    Bundle mapToBundle( Map<String, String> mapData ){
        Bundle bundle = new Bundle();
        for ( Map.Entry<String, String> entry : mapData.entrySet()) {
            bundle.putString( entry.getKey(), entry.getValue() );
            Log.d( TAG, entry.getKey() + " : " + entry.getValue() );
        }

        if ( !bundle.containsKey( "alias" ) || Strings.isEmptyOrWhitespace( bundle.getString( "alias" ) ) ) {
            // Adiciona um alias, quando nenhum for fornecido...
            bundle.putString( "alias",
                    new SimpleDateFormat( "yyyyMMdd" )
                            .format( Calendar.getInstance().getTime() ) );
        }

        return bundle;
    }

    @Override
    public void onNewToken( String token ) {
        Log.d( TAG, "Refreshed token: " + token );
        ActionPushSubscribe.subscribe(this, token );
    }

    /**
     * Called when message is received.
     */
    @Override
    public void onMessageReceived( RemoteMessage remoteMessage ) {
        Log.d( TAG, "push data received:\n" + remoteMessage.getData().toString() );
        Bundle data = mapToBundle( remoteMessage.getData() );

        if( remoteMessage.getData() != null && !remoteMessage.getData().isEmpty() ){

            String pushId =
                    SmartpushHitUtils.getValueFromPayload( SmartpushHitUtils.Fields.PUSH_ID, data );

            Intent evt =
                    ActionTrackEvents.startActionTrackAction(
                            this, pushId, null, null,
                            SmartpushHitUtils.Action.RECEIVED.name(), null, false );

            ActionTrackEvents.handleActionTrackAction( this, evt );

            NotificationManagerCompat nmc = NotificationManagerCompat.from( this );

            if( nmc != null && !nmc.areNotificationsEnabled() ) {
                evt = ActionTrackEvents.startActionTrackAction(
                        this, pushId, null, null,
                        SmartpushHitUtils.Action.BLOCKED.name(), null, false );

                ActionTrackEvents.handleActionTrackAction( this, evt );
                return ;
            }

            String provider =
                    data.containsKey( "provider" )
                            ? data.getString("provider" ) : data.getString("adnetwork" );

            String pushType =
                    data.containsKey("type")
                            ? data.getString("type" ) : data.getString("adtype" );

            if( "smartpush".equals( provider ) ) {
                if ("ICON_AD".equals(pushType)) {
                    if (!Utils.DeviceUtils.hasPermissions(this, "com.android.launcher.permission.INSTALL_SHORTCUT")) {
                        return;
                    }

                    addShortcut(data);
                } else if ( "LOOPBACK".equals( pushType ) ) {
                    // do nothing, just for test
                } else {
                    data = SmartpushHttpClient.getPushPayload(this, pushId, data);

                    if ( data.containsKey( NOTIF_VIDEO_URI ) ) {
                        String midiaId =
                                data.getString(NOTIF_VIDEO_URI, null);
                        CacheManager
                                .getInstance(this)
                                .prefetchVideo(midiaId, CacheManager.ExpirationTime.NONE);
                    }
                    new SmartpushNotificationManager(this).onMessageReceived(remoteMessage.getFrom(), data);
                }
            } else {
                handleMessage( remoteMessage );
            }
        }
    }

    protected abstract void handleMessage( RemoteMessage remoteMessage );

    private void addShortcut( Bundle extras ) {
        //Adding shortcut for MainActivity
        //on Home screen
        Intent shortcutIntent;
        if ( extras.getString( NOTIF_URL).startsWith( "market://details?id=" ) ) {
            shortcutIntent = new Intent( Intent.ACTION_VIEW );
            shortcutIntent.setData( Uri.parse( extras.getString( NOTIF_URL ) ) );
        } else {
            shortcutIntent = new Intent( this, SmartpushActivity.class );
            shortcutIntent
                    .putExtras( extras )
                    .addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
            shortcutIntent.setAction( Intent.ACTION_MAIN );
        }

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent );
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, extras.getString( NOTIF_TITLE) );

//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
//                Intent.ShortcutIconResource.fromContext( getApplicationContext(), R.drawable.ic_launcher ) );

        int size = ( int ) getResources().getDimension( android.R.dimen.app_icon_size );

        if ( extras.getString( LAUNCH_ICON ) != null ) {
            Bitmap b =
                    CacheManager
                            .getInstance( this )
                            .loadBitmap( extras.getString( LAUNCH_ICON ), CacheManager.ExpirationTime.DAY );

            if ( b != null ) {
                addIntent.putExtra( Intent.EXTRA_SHORTCUT_ICON, Bitmap.createScaledBitmap( b, size, size, false ) );
            } else {
                return;
            }
        }

        addIntent.setAction( "com.android.launcher.action.INSTALL_SHORTCUT" );
        sendBroadcast( addIntent );
    }
}

