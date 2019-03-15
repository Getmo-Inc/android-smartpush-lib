package br.com.getmo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import br.com.smartpush.SmartpushMessagingListenerService;

public class MySmartpushMessageService extends SmartpushMessagingListenerService {

    @Override
    protected void handleMessage( RemoteMessage remoteMessage ) {
        // Custom notification implementation
        Log.d( "DEBUG-NOT", "push custom" );
        Bundle bundle = mapToBundle(remoteMessage.getData());
        sendNotification(bundle);
    }

    Bundle mapToBundle( Map<String, String> mapData ){
        Bundle bundle = new Bundle();
        for ( Map.Entry<String, String> entry : mapData.entrySet()) {
            bundle.putString( entry.getKey(), entry.getValue() );
        }
        return bundle;
    }

    private void sendNotification( Bundle extras ) {
        Log.d( "LOG", "Notificacao delegada ao dev." );
        Intent intent = new Intent( this, MainActivity.class );
        intent.putExtras( extras );
        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        PendingIntent pendingIntent =
                PendingIntent
                        .getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder notificationBuilder = new Notification.Builder( this )
                .setSmallIcon( R.drawable.ic_getmo )
                .setContentTitle( "Push Notification!" )
                .setContentText( extras.getString("detail") )
                .setAutoCancel( true )
                .setSound( defaultSoundUri )
                .setContentIntent( pendingIntent );

        NotificationManager notificationManager =
                ( NotificationManager ) getSystemService( Context.NOTIFICATION_SERVICE );

        notificationManager.notify( 1000 /* ID of notification */, notificationBuilder.build() );
    }

//    @Override
//    public void onMessageReceived(RemoteMessage remoteMessage) {
//        Log.d("DEBUG-NOT", "onMessageReceived");
//        handleMessage(remoteMessage);
//    }
}
