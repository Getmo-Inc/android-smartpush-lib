package br.com.smartpush;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.Date;

import static br.com.smartpush.Utils.TAG;


/**
 * Created by fabio.licks on 12/02/16.
 */
public final class Smartpush {

//    private static int smallPushIcon = R.drawable.ic_notif_getmo;
//    private static int largePushIcon = R.drawable.ic_getmo;
//
//    public static void setSmallPushIcon( int drawableId ) {
//        smallPushIcon = drawableId;
//    }
//
//    public static void setLargePushIcon( int drawableId ) {
//        largePushIcon = drawableId;
//    }
//
//    public static int getSmallPushIcon( ) {
//        return smallPushIcon;
//    }
//
//    public static int getLargePushIcon( ) {
//        return largePushIcon;
//    }

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
                    SmartpushService.startActionBlockPush(context, block);
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
            SmartpushService.startActionGetDeviceUserInfo(context);
        }
    }

    public static void nearestZone(final Context context, final double lat, final double lng) {
        if ( isRegistered( context ) ) {
            SmartpushService.startActionNearestZone(context, lat, lng);
        }
    }

    public static void setTag(final Context context, final String key, final Boolean value) {
        if ( isRegistered( context ) && value != null )
            SmartpushService.startActionSetTag(context, key, value);
    }

    public static void setTag(final Context context, final String key, final Double value) {
        if ( isRegistered( context ) && value != null )
            SmartpushService.startActionSetTag(context, key, value);
    }

    public static void setTag(final Context context, final String key, final ArrayList<String> values) {
        if ( isRegistered( context ) && values != null && values.size() > 0 )
            SmartpushService.startActionSetTag(context, key, values);
    }

    public static void setTag( final Context context, final String key, final String value ) {
        if ( isRegistered( context ) && value != null )
            SmartpushService.startActionSetTag(context, key, value);
    }

    public static void setTag( final Context context, final String key, final Date value ) {
        if ( isRegistered( context ) && value != null )
            SmartpushService.startActionSetTag(context, key, value);
    }

    public static void delTagOrValue(final Context context, final String key, final Boolean value) {
        if ( isRegistered( context ) ) {
            SmartpushService.startActionDelTagOrValue(context, key, value);
        }
    }

    public static void delTagOrValue(final Context context, final String key, final Double value) {
        if ( isRegistered( context ) ) {
            SmartpushService.startActionDelTagOrValue(context, key, value);
        }
    }

    public static void delTagOrValue(final Context context, final String key, final ArrayList<String> values) {
        if ( isRegistered( context ) ) {
            SmartpushService.startActionDelTagOrValue(context, key, values);
        }
    }

    public static void delTagOrValue( final Context context, final String key, final String value ) {
        if ( isRegistered( context ) ) {
            SmartpushService.startActionDelTagOrValue(context, key, value);
        }
    }

    public static void delTagOrValue( final Context context, final String key, final Date value ) {
        if ( isRegistered( context ) ) {
            SmartpushService.startActionDelTagOrValue(context, key, value);
        }
    }

    public static void hit( final Context context, String pushId, String screenName, String category, String action, String label ) {
        if ( isRegistered( context ) ) {
            SmartpushService.startActionTrackAction(context, pushId, screenName, category, action, label);
        }
    }

    public static void hit( final Context context, String pushId, String screenName, String category, SmartpushHitUtils.Action action, String label ) {
        if ( isRegistered( context ) ) {
            SmartpushService.startActionTrackAction( context, pushId, screenName, category, action.name(), label );
        }
    }

    public static void hit( final Context context, String screenName, String category, String action, String label ) {
        if ( isRegistered(context) ) {
            SmartpushService.startActionTrackAction(context, null, screenName, category, action, label);
        }
    }

    public static void getLastMessages( final Context context, Date startingDate ) {
        if ( isRegistered(context) ) {
            SmartpushService.startActionLastMessages( context, startingDate );
        }
    }

    public static void getLastUnreadMessages( final Context context, Date startingDate ) {
        if ( isRegistered(context) ) {
            SmartpushService.startActionLastUnreadMessage( context, startingDate );
        }
    }

    public static void markMessageAsRead( final Context context, String pushId ) {
        if ( isRegistered(context) ) {
            SmartpushService.startActionMarkMessageAsRead( context, pushId );
        }
    }

    public static void markAllMessagesAsRead( final Context context ) {
        if ( isRegistered(context) ) {
            SmartpushService.startActionMarkAllMessagesAsRead( context );
        }
    }

    public static void getMessageExtraPayload( final Context context, String pushId ) {
        if ( isRegistered(context) ) {
            SmartpushService.startActionGetMessageExtraPayload( context, pushId );
        }
    }

    public static void subscribe( final Context context, String yourGooglePlayServiceProjectId ) {
        subscribe( context );
    }

    public static String getGeozones( final Context context ) {
        SQLiteDatabase db =
                new DatabaseManager( context ).getWritableDatabase();

        String jsonArray = ( String ) GeozoneDAO.listAllToJSONString( db );

        db.close();

        return jsonArray;
    }

    public static void subscribe( final Context context ) {
        if ( Smartpush.checkPlayServices( context ) ) {
            if ( checkSmartpush( context ) ) {
                SmartpushService.subscrive( context );
                SmartpushService.getMsisdn( context );
                SmartpushService.getMccMnc( context );
//                SmartpushService.getAppList( context );
            }
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK.
     */
    private static boolean checkPlayServices( Context _c ) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable( _c );
        return ( resultCode == ConnectionResult.SUCCESS );
    }

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
        return  (  Utils.PreferenceUtils.readFromPreferences( context, Utils.Constants.SMARTP_REGID ) != null
                && Utils.PreferenceUtils.readFromPreferences( context, Utils.Constants.SMARTP_ALIAS ) != null
                && Utils.PreferenceUtils.readFromPreferences( context, Utils.Constants.SMARTP_HWID  ) != null );
    }

    public static String printVersion() {
        return "Smartpush - version " + BuildConfig.VERSION_NAME ;
    }
}
