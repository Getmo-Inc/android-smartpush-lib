package br.com.smartpush;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */

public final class SmartpushService extends IntentService {

//    // Smartpush PROJECT ID
//    private static final String PLAY_SERVICE_INTERNAL_PROJECT_ID = "520757792663";

    public static final int SERVICE_ID = 456123;

    public SmartpushService() {
        super("SmartpushService");
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        if ( intent != null ) {
            final String action = intent.getAction();
            final Context context = getApplicationContext();

            switch ( action ) {
                case ActionPushSubscribe.ACTION_REGISTRATION:
                    String token = intent.getStringExtra( ActionPushSubscribe.ACTION_REGISTRATION );
                    ActionPushSubscribe.sendRegistrationToServer( context, token );
                    return;
                case ActionTagManager.ACTION_SET_TAG:
                    ActionTagManager.handleActionSetOrDeleteTag( context, intent );
                    return;
                case ActionTagManager.ACTION_GET_TAG:
                    ActionTagManager.handleActionGetTagValues( context, intent );
                    return;
                case ActionPushBlock.ACTION_BLOCK_PUSH:
                    ActionPushBlock.handleActionBlockPush( context, intent );
                    return;
                case Smartpush.ACTION_GET_DEVICE_USER_INFO:
                    ActionGetDeviceInfo.handleActionGetDeviceUserInfo( context, intent );
                    return;
                case ActionNearestzone.ACTION_NEARESTZONE:
                    ActionNearestzone.handleActionNearestZone( context, intent );
                    return;
                case ActionTrackEvents.ACTION_TRACK_ACTION:
                    ActionTrackEvents.handleActionTrackAction( context, intent );
                    return;
                case Smartpush.ACTION_LAST_10_NOTIF:
                    ActionPushInbox.handleActionLastMessages( context, intent );
                    return;
                case Smartpush.ACTION_LAST_10_UNREAD_NOTIF:
                    ActionPushInbox.handleActionLastUnreadMessages( context, intent );
                    return;
                case Smartpush.ACTION_MARK_NOTIF_AS_READ:
                    ActionPushInbox.handleActionMarkMessageAsRead( context, intent );
                    return;
                case ActionPushInbox.ACTION_HIDE_NOTIF:
                    ActionPushInbox.handleActionHideMessage( context, intent );
                    return;
                case Smartpush.ACTION_MARK_ALL_NOTIF_AS_READ:
                    ActionPushInbox.handleActionMarkAllMessagesAsRead( context, intent );
                    return;
                case Smartpush.ACTION_GET_NOTIF_EXTRA_PAYLOAD:
                    ActionPushInbox.handleActionGetMessageExtraPayload( context, intent );
                    return;
                case ActionGetMsisdn.ACTION_GET_MSISDN:
                    ActionGetMsisdn.getMsisdn( context );
                    return;
                case ActionGetAppList.ACTION_GET_APP_LIST:
                    ActionGetAppList.handleActionSaveAppsListState( context );
                    return;
                case ActionPushManager.ACTION_NOTIF_UPDATABLE:
                case ActionPushManager.ACTION_NOTIF_UPDATABLE_NEXT:
                case ActionPushManager.ACTION_NOTIF_UPDATABLE_PREV:
                    new SmartpushNotificationManager( context )
                            .onMessageReceived( null, intent.getExtras() );
                    return;
                case ActionPushManager.ACTION_NOTIF_CANCEL:
                    ActionPushManager.handleActionCancelNotification( context, intent.getExtras() );
                    return;
                case ActionPushManager.ACTION_NOTIF_REDIRECT:
                    ActionPushManager
                            .handleActionRedirectNotification(
                                    context, ActionPushManager.closeNotificationCenter( context, intent ) );
                    return;
            }
        }
    }

    static void start(Intent intent, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}