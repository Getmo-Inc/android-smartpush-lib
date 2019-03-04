package br.com.smartpush;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import static br.com.smartpush.Utils.Constants.NOTIF_PACKAGENAME;
import static br.com.smartpush.Utils.Constants.NOTIF_URL;
import static br.com.smartpush.Utils.Constants.PUSH_INTERNAL_ID;
import static br.com.smartpush.Utils.TAG;

class ActionPushManager {
    // RICH NOTIFICATION
    public  static final String ACTION_NOTIF_UPDATABLE = "action.UPDATABLE";
    public  static final String ACTION_NOTIF_UPDATABLE_NEXT = "action.UPDATABLE_NEXT";
    public  static final String ACTION_NOTIF_UPDATABLE_PREV = "action.UPDATABLE_PREV";
    public  static final String ACTION_NOTIF_CANCEL = "action.CANCEL";
    public  static final String ACTION_NOTIF_REDIRECT = "action.REDIRECT";

    public static void handleActionRedirectNotification( Context context, Bundle data ) {

        // TODO MUTABLE PUSH NOTIFICATION
//        // Configure PendingIntent to Cancel refresh
//        Intent serviceIntent =
//                new Intent( this, SmartpushService.class)
//                        .setAction( ACTION_NOTIF_UPDATABLE )
//                        .putExtras( extras );
//
//        PendingIntent servicePendingIntent =
//                PendingIntent.getService( this,
//                        // integer constant used to identify the service
//                        SmartpushService.SERVICE_ID,
//                        serviceIntent,
//                        // FLAG to avoid creating a second service if there's already one running
//                        PendingIntent.FLAG_CANCEL_CURRENT );
//
//        /** this gives us the time for the first trigger. */
//        AlarmManager am = ( AlarmManager ) getSystemService( Context.ALARM_SERVICE );
//        am.cancel( servicePendingIntent );
//        SmartpushLog.d( TAG, "-------------------> REFRESH CANCELED." );

        //
        String action =
                ( data.containsKey( NOTIF_URL ) )
                        ? data.getString( NOTIF_URL ) : data.getString( "link" );

        String packageName = data.getString( NOTIF_PACKAGENAME );

        Intent intent =
                Utils.Smartpush.getIntentToRedirect( context, action, packageName, data );

        if ( intent != null && intent.resolveActivity( context.getPackageManager()) != null ) {
            context.startActivity( intent );
        }

        // Hit notification clicked!
        String pushId =
                SmartpushHitUtils.getValueFromPayload(
                        SmartpushHitUtils.Fields.PUSH_ID, data );

        String label = null;
        if ( data.containsKey( "frame.current" ) ) {
            int slideClickedId = data.getInt( "frame.current", 0 );

            String extras =
                    ( data.containsKey( Utils.Constants.PUSH_EXTRAS )
                            ? data.getString( Utils.Constants.PUSH_EXTRAS ) : null );

            if ( extras != null ) {
                JSONObject payloadExtra = null;
                try {
                    payloadExtra = new JSONObject( extras );
                    label = payloadExtra.getString( "frame:" + ( slideClickedId + 1 ) + ":url" );
                } catch ( JSONException e ) {
                    SmartpushLog.e( Utils.TAG, e.getMessage(), e );
                }
            }
        }

        Intent evt =
                ActionTrackEvents.startActionTrackAction(
                        context, pushId, null, null, SmartpushHitUtils.Action.CLICKED.name(), label, false );
        ActionTrackEvents.handleActionTrackAction( context, evt );

// TODO remover após testes
//        startActionTrackAction( context, pushId, null, null, SmartpushHitUtils.Action.CLICKED.name(), label );
//        SmartpushHttpClient.sendToAnalytics( context, pushId,  SmartpushHitUtils.Action.CLICKED.name() );

        SmartpushLog.d( TAG,
                "-------------------> APP OPENED FROM NOTIFICATION. - " + pushId );
    }

    public static void handleActionCancelNotification( Context context, Bundle extras ) {
        // Hit notification canceled!
        String pushId =
                SmartpushHitUtils.getValueFromPayload(
                        SmartpushHitUtils.Fields.PUSH_ID, extras );

        Intent evt =
                ActionTrackEvents.startActionTrackAction(
                        context, pushId, null, null, SmartpushHitUtils.Action.REJECTED.name(), null, false );
        ActionTrackEvents.handleActionTrackAction( context, evt );

// TODO remover após testes
//        startActionTrackAction( context, pushId, null, null, SmartpushHitUtils.Action.REJECTED.name(), null  );

        SmartpushLog.d( TAG, "-------------------> NOTIFICATION REJECTED. - " + pushId );

        // TODO MUTABLE PUSH NOTIFICATION
//        // Configure PendingIntent to Cancel refresh
//        Intent serviceIntent =
//                new Intent( this, SmartpushService.class)
//                        .setAction( ACTION_NOTIF_UPDATABLE )
//                        .putExtras( extras );
//
//        PendingIntent servicePendingIntent =
//                PendingIntent.getService( this,
//                        // integer constant used to identify the service
//                        SmartpushService.SERVICE_ID,
//                        serviceIntent,
//                        // FLAG to avoid creating a second service if there's already one running
//                        PendingIntent.FLAG_CANCEL_CURRENT );
//
//        /** this gives us the time for the first trigger. */
//        AlarmManager am = ( AlarmManager ) getSystemService( Context.ALARM_SERVICE );
//        am.cancel( servicePendingIntent );
//        SmartpushLog.d( TAG, "-------------------> REFRESH CANCELED." );
    }

    public static Bundle closeNotificationCenter( Context context, Intent intent ) {
        Bundle data = intent.getExtras();

        if ( data != null && SmartpushNotificationManager.isAutoCancel( data ) ) {
            NotificationManager manager =
                    ( NotificationManager ) context.getSystemService( Context.NOTIFICATION_SERVICE );

            // Cancels the notification
            manager.cancel( PUSH_INTERNAL_ID );
        }

        // Close notification bar!
        context.sendBroadcast( new Intent( Intent.ACTION_CLOSE_SYSTEM_DIALOGS ) );

        return data;
    }
}
