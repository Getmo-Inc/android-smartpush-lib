package br.com.smartpush;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.util.Strings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.Date;

import static br.com.smartpush.Utils.TAG;

/**
 * Created by fabio.licks on 12/02/16.
 */
public final class Smartpush {
    public static final String PROVIDER = "smartpush";

    public static final String ACTION_REGISTRATION_RESULT = "action.REGISTRATION_RESULT";
    public static final String ACTION_GET_TAG_VALUES = "action.GET_TAG_VALUES";
    public static final String ACTION_GEOZONES_UPDATED = "action.GEOZONES_UPDATED";
    public static final String ACTION_GET_DEVICE_USER_INFO = "action.GET_DEVICE_USER_INFO";

    // params
    public static final String EXTRA_VALUE  = "extra.VALUE";
    public static final String EXTRA_DEVICE_INFO = "extra.EXTRA_DEVICE_INFO";

    // INBOX
    public  static final String ACTION_LAST_10_UNREAD_NOTIF = "action.LAST_10_UNREAD_NOTIF";
    public  static final String ACTION_LAST_10_NOTIF = "action.LAST_10_NOTIF";
    public  static final String ACTION_MARK_NOTIF_AS_READ = "action.MARK_NOTIF_AS_READ";
    public  static final String ACTION_MARK_ALL_NOTIF_AS_READ = "action.MARK_ALL_NOTIF_AS_READ";
    public  static final String ACTION_GET_NOTIF_EXTRA_PAYLOAD = "action.GET_NOTIF_EXTRA_PAYLOAD";

    public static boolean areNotificationsEnabled( final Context context ) {
        NotificationManagerCompat nmc = NotificationManagerCompat.from( context );
        if ( nmc != null ) {
            return nmc.areNotificationsEnabled();
        }

        return true;
    }

    public static boolean blockPush( final Context context, final boolean block ) {
        if ( isRegistered( context ) ) {
            NotificationManagerCompat nmc = NotificationManagerCompat.from( context );
            if ( nmc != null ) {
                if ( nmc.areNotificationsEnabled() ) {
                    ActionPushBlock.startActionBlockPush( context, block );
                    return !block;
                } else {
                    SmartpushLog.d( TAG, "The Notification has been blocked by the S.O." );
                    return true;
                }
            }
        }
        return block;
    }

    public static void getUserInfo( final Context context ) {
        if ( isRegistered( context ) ) {
            ActionGetDeviceInfo.startActionGetDeviceUserInfo( context );
        }
    }

    public static void nearestZone(final Context context, final double lat, final double lng) {
        if ( isRegistered( context ) ) {
            ActionNearestzone.startActionNearestZone( context, lat, lng );
        }
    }

    public static void setTag(final Context context, final String key, final Boolean value) {
        if ( isRegistered( context ) && value != null )
            ActionTagManager.startActionSetTag( context, key, value );
    }

    public static void setTag(final Context context, final String key, final Double value) {
        if ( isRegistered( context ) && value != null )
            ActionTagManager.startActionSetTag( context, key, value );
    }

    public static void setTag(final Context context, final String key, final ArrayList<String> values) {
        if ( isRegistered( context ) && values != null && values.size() > 0 )
            ActionTagManager.startActionSetTag( context, key, values );
    }

    public static void setTag( final Context context, final String key, final String value ) {
        if ( isRegistered( context ) && value != null )
            ActionTagManager.startActionSetTag( context, key, value );
    }

    public static void setTag( final Context context, final String key, final Date value ) {
        if ( isRegistered( context ) && value != null )
            ActionTagManager.startActionSetTag( context, key, value );
    }

    public static void delTagOrValue(final Context context, final String key, final Boolean value) {
        if ( isRegistered( context ) ) {
            ActionTagManager.startActionDelTagOrValue( context, key, value );
        }
    }

    public static void delTagOrValue(final Context context, final String key, final Double value) {
        if ( isRegistered( context ) ) {
            ActionTagManager.startActionDelTagOrValue( context, key, value );
        }
    }

    public static void delTagOrValue(final Context context, final String key, final ArrayList<String> values) {
        if ( isRegistered( context ) ) {
            ActionTagManager.startActionDelTagOrValue( context, key, values );
        }
    }

    public static void delTagOrValue( final Context context, final String key, final String value ) {
        if ( isRegistered( context ) ) {
            ActionTagManager.startActionDelTagOrValue( context, key, value );
        }
    }

    public static void delTagOrValue( final Context context, final String key, final Date value ) {
        if ( isRegistered( context ) ) {
            ActionTagManager.startActionDelTagOrValue( context, key, value );
        }
    }

    public static void getTagValues(final Context context, final String key){
        if (isRegistered( context ) ) {
            ActionTagManager.startActionGetTagValues( context, key );
        }
    }

    public static void hit( final Context context, String pushId, String screenName, String category, String action, String label ) {
        if ( isRegistered( context ) ) {
            ActionTrackEvents.startActionTrackAction( context, pushId, screenName, category, action, label, true );
        }
    }

    public static void hit( final Context context, String pushId, String screenName, String category, SmartpushHitUtils.Action action, String label ) {
        if ( isRegistered( context ) ) {
            ActionTrackEvents.startActionTrackAction( context, pushId, screenName, category, action.name(), label, true );
        }
    }

    public static void hit( final Context context, String screenName, String category, String action, String label ) {
        if ( isRegistered(context) ) {
            ActionTrackEvents.startActionTrackAction( context, null, screenName, category, action, label, true );
        }
    }

    public static void getLastMessages( final Context context, Date startingDate ) {
        if ( isRegistered(context) ) {
            ActionPushInbox.startActionLastMessages( context, startingDate );
        }
    }

    public static void getLastUnreadMessages( final Context context, Date startingDate ) {
        if ( isRegistered(context) ) {
            ActionPushInbox.startActionLastUnreadMessage( context, startingDate );
        }
    }

    public static void markMessageAsRead( final Context context, String pushId ) {
        if ( isRegistered(context) ) {
            ActionPushInbox.startActionMarkMessageAsRead( context, pushId );
        }
    }

    public static void deleteMessage( final Context context, String pushId ) {
        if ( isRegistered( context ) ) {
            ActionPushInbox.startActionHideMessage( context, pushId );
        }
    }

    public static void markAllMessagesAsRead( final Context context ) {
        if ( isRegistered(context) ) {
            ActionPushInbox.startActionMarkAllMessagesAsRead( context );
        }
    }

    public static void getMessageExtraPayload( final Context context, String pushId ) {
        if ( isRegistered(context) ) {
            ActionPushInbox.startActionGetMessageExtraPayload( context, pushId );
        }
    }

    public static String getGeozones( final Context context ) {
        SQLiteDatabase db =
                new DatabaseManager( context ).getWritableDatabase();

        String jsonArray = ( String ) GeozoneDAO.listAllToJSONString( db );

        db.close();

        return jsonArray;
    }

// TODO revisar e remover ...
    public static void subscribe( final Context context ) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener( new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete( @NonNull Task<InstanceIdResult> task ) {
                        if ( !task.isSuccessful() ) {
                            Log.w( TAG, "getInstanceId failed", task.getException() );
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        Log.d( TAG, "------------------> " + token );
                        ActionPushSubscribe.subscribeByService( context, token );
                    }
                });
    }

//    /**
//     * Check the device to make sure it has the Google Play Services APK.
//     */
//    private static boolean checkPlayServices( Context _c ) {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        int resultCode = apiAvailability.isGooglePlayServicesAvailable( _c );
//        return ( resultCode == ConnectionResult.SUCCESS );
//    }

    private static boolean checkSmartpush(Context context) {
        SmartpushLog.d( TAG, "Smartpush SDK : " + printVersion() );
        SmartpushLog.d( TAG, "checkSmartpush() : begin - Configurations tests : " + context.getPackageName() );

        if ( Utils.Smartpush.getMetadata( context, Utils.Constants.SMARTP_API_KEY) == null ) {
            throw new SmartpushException(
                    "Metadata not found! Add \"" + Utils.Constants.SMARTP_API_KEY + "\" to your manifest file!" );
        }

        if ( Utils.Smartpush.getMetadata( context, Utils.Constants.SMARTP_APP_ID) == null ) {
            throw new SmartpushException(
                    "Metadata not found! Add \"" + Utils.Constants.SMARTP_APP_ID + "\" to your manifest file!" );
        }

        SmartpushLog.d( TAG, "checkSmartpush() : Metadata, pass!" );

        try {
            PackageInfo packageInfo =
                    context.getPackageManager().getPackageInfo(
                            context.getPackageName(), PackageManager.GET_ACTIVITIES);

            ActivityInfo[] activities = packageInfo.activities;
            boolean found = false;
            for ( int i = 0, j = activities.length; i < j; i++ ) {
                if ( "br.com.smartpush.SmartpushActivity".equals( activities[ i ].name ) ) {

                    ActivityInfo ac = activities[i];

                    if ( ac.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED ) {
                        throw new SmartpushException(
                                "[br.com.smartpush.SmartpushActivity] ScreenOrientation activity atribute found! Please, remove android:screenOrientation atribute from activity manifest tag!");
                    }

                    if ( ac.launchMode != ActivityInfo.LAUNCH_SINGLE_TASK ) {
                        throw new SmartpushException(
                                "[br.com.smartpush.SmartpushActivity] LaunchMode activity atribute not found! Add android:launchMode=\"singleTask\" to activity manifest tag!");
                    }

                    if ( ac.taskAffinity != null ) {
                        throw new SmartpushException(
                                "[br.com.smartpush.SmartpushActivity] TaskAffinity activity atribute is wrong! Please, set android:taskAffinity=\"\" to activity manifest tag!");
                    }

                    if ( ( ac.flags & ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS ) != ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS ) {
                        throw new SmartpushException(
                                "[br.com.smartpush.SmartpushActivity] ExcludeFromRecents activity atribute is wrong! Please, set android:excludeFromRecents=\"true\" to activity manifest tag!");
                    }

                    found = true;
                }
            }

            if ( !found ) {
                throw new SmartpushException(
                        "Activity not found! Add \"br.com.smartpush.SmartpushActivity\" to your manifest file!");
            }

            SmartpushLog.d( TAG, "checkSmartpush() : Activity, pass!" );

        } catch ( PackageManager.NameNotFoundException e ) {
            // should never happen
            throw new RuntimeException( "Could not get package name: " + e );
        }

        SmartpushLog.d( TAG, "checkSmartpush() : end - Configurations tests : " + context.getPackageName() );

        return true;
    }

    private static boolean isRegistered( Context context ) {
        return !Strings.isEmptyOrWhitespace( Utils.PreferenceUtils.readFromPreferences( context, Utils.Constants.SMARTP_REGID ) )
                && !Strings.isEmptyOrWhitespace( Utils.PreferenceUtils.readFromPreferences( context, Utils.Constants.SMARTP_ALIAS ) )
                && !Strings.isEmptyOrWhitespace( Utils.PreferenceUtils.readFromPreferences( context, Utils.Constants.SMARTP_HWID ) );
    }

    public static String printVersion() {
        return "Smartpush - version " + BuildConfig.VERSION_NAME ;
    }
}
