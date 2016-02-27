package br.com.getmo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import br.com.smartpush.SmartpushListenerService;

/**
 * Created by leticia on 10/02/16.
 */
public class MySmartpushListenerService extends SmartpushListenerService {

    @Override
    protected void handleMessage( Bundle data ) {
        String message = data.getString( "detail" );

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

        sendNotification( message, data );
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification( String message, Bundle extras ) {
        Intent intent = new Intent( this, MainActivity.class );
        intent.putExtras( extras );
        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        PendingIntent pendingIntent =
                PendingIntent
                        .getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder( this )
                .setSmallIcon( R.drawable.ic_stat_ic_notification )
                .setContentTitle( "Push Notification!" )
                .setContentText( message )
                .setAutoCancel( true )
                .setSound( defaultSoundUri )
                .setContentIntent( pendingIntent );

        NotificationManager notificationManager =
                ( NotificationManager ) getSystemService( Context.NOTIFICATION_SERVICE );

        notificationManager.notify( 1000 /* ID of notification */, notificationBuilder.build() );
    }
}
