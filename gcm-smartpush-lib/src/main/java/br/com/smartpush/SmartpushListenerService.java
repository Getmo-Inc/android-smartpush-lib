package br.com.smartpush;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.gcm.GcmListenerService;

import static br.com.smartpush.Utils.Constants.LAUNCH_ICON;
import static br.com.smartpush.Utils.Constants.NOTIF_TITLE;
import static br.com.smartpush.Utils.Constants.NOTIF_URL;
import static br.com.smartpush.Utils.Constants.NOTIF_VIDEO_URI;

/**
 * Created by fabio.licks on 10/02/16.
 */
public abstract class SmartpushListenerService extends GcmListenerService {
    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived( String from, Bundle data ) {

        if ( data != null && !data.isEmpty() ) {
            // 1. tracking push RECEIVED
            String pushId =
                    SmartpushHitUtils.getValueFromPayload(
                            SmartpushHitUtils.Fields.PUSH_ID, data );

            Smartpush.hit( this, pushId, null, null, SmartpushHitUtils.Action.RECEIVED, null );

            // 2. is it blocked? If yes abort notification...
            NotificationManagerCompat nmc = NotificationManagerCompat.from( this );
            if ( nmc != null ) {
                if ( !nmc.areNotificationsEnabled() ) {
                    // CANCEL NOTIFICATION
                    return;
                }
            }

            // 3.
            String provider  =
                    ( data.containsKey( "provider" ) )
                            ? data.getString( "provider" )
                            : data.getString( "adnetwork" );

            String pushType  =
                    ( data.containsKey( "type" ) )
                            ? data.getString( "type" )
                            : data.getString( "adtype" );

            if ( "smartpush".equals( provider ) ) {
                if ( "ICON_AD".equals( pushType ) ) {
                    if ( !Utils.DeviceUtils.hasPermissions( this, "com.android.launcher.permission.INSTALL_SHORTCUT" ) ) {
                        // CANCEL SHORTCUT INSTALLATION
                        return;
                    }

                    addShortcut( data );

                    // Tracking
                    Smartpush.hit( this, pushId, null, null, SmartpushHitUtils.Action.INSTALLED, null);

                } else if ( "LOOPBACK".equals( pushType ) ) {

//                    // Tracking
//                    Smartpush.hit( this, pushId, null, null, SmartpushHitUtils.Action.ONLINE, null );

                    // 2. Update status - optin/optout
//                    if ( nmc != null ) {
//                        Smartpush.blockPush( this, !nmc.areNotificationsEnabled() );
//                    }

                } else {
                    // Retrieve updated payload
                    data = SmartpushHttpClient.getPushPayload( this, pushId, data );

                    // If has "video" attribute in bundle prefetch
                    if ( data.containsKey( NOTIF_VIDEO_URI ) ) {
                        // Prefetching video...
                        String midiaId =
                                data.getString( NOTIF_VIDEO_URI, null );

                        CacheManager
                                .getInstance( this )
                                .prefetchVideo( midiaId, CacheManager.ExpirationTime.NONE );
                    }

                    // RICH NOTIFICATION
                    new SmartpushNotificationManager( this ).onMessageReceived( from, data );
                }
            } else {
                // by pass to developer
                handleMessage( data );
            }
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        // sendNotification(message);
        // [END_EXCLUDE]
    }

    protected abstract void handleMessage( Bundle data );

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

