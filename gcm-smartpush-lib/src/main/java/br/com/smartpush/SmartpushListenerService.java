package br.com.smartpush;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.gcm.GcmListenerService;

import static br.com.smartpush.SmartpushNotificationManager.LAUNCH_ICON;

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
            // Tracking
            String pushId =
                    SmartpushHitUtils.getValueFromPayload(
                            SmartpushHitUtils.Fields.PUSH_ID, data );

            Smartpush.hit( this, pushId, null, null, SmartpushHitUtils.Action.RECEIVED, null );

            String pushType  =
                    ( data.containsKey( "type" ) )
                            ? data.getString( "type" )
                            : data.getString( "adtype" );

            String provider  =
                    ( data.containsKey( "provider" ) )
                            ? data.getString( "provider" )
                            : data.getString( "adnetwork" );

            if ( "smartpush".equals( provider ) ) {
                if ( "ICON_AD".equals( pushType ) ) {

                    int permissionCheck =
                            ContextCompat.checkSelfPermission( this, "com.android.launcher.permission.INSTALL_SHORTCUT" );

                    if ( permissionCheck != PackageManager.PERMISSION_GRANTED ) {
                        // CANCEL SHORTCUT
                        return;
                    }

                    // Tracking
                    Smartpush.hit( this, pushId, null, null, SmartpushHitUtils.Action.INSTALLED, null);

                    addShortcut( data );

                } else if ( "LOOPBACK".equals( pushType ) ) {

                    // Tracking
                    Smartpush.hit( this, pushId, null, null, SmartpushHitUtils.Action.ONLINE, null );

                } else {
                    new SmartpushNotificationManager( this ).onMessageReceived( from, data );
                }
            } else {
                // by pass
                if ( this instanceof SmartpushListenerService ) {
                    ( ( SmartpushListenerService )this ).handleMessage( data );
                }
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
        if ( extras.getString( SmartpushNotificationManager.NOTIF_URL).startsWith( "market://details?id=" ) ) {
            shortcutIntent = new Intent( Intent.ACTION_VIEW );
            shortcutIntent.setData( Uri.parse(extras.getString( SmartpushNotificationManager.NOTIF_URL) ) );
        } else {
            shortcutIntent = new Intent( this, SmartpushActivity.class );
            shortcutIntent
                    .putExtras( extras )
                    .addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
            shortcutIntent.setAction( Intent.ACTION_MAIN );
        }

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent );
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, extras.getString( SmartpushNotificationManager.NOTIF_TITLE) );

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

