package br.com.smartpush;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.Date;


/**
 * Created by fabio.licks on 12/02/16.
 */
public final class Smartpush {

    public static boolean blockPush( final Context context, final boolean block ) {
        if ( isRegistered( context ) ) {
            SmartpushService.startActionBlockPush(context, block);
        }
        return !block;
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

    public static void hitClick( final Context context, Bundle bundle ) {
        if ( isRegistered( context ) ) {
            if ( bundle != null && bundle.containsKey( SmartpushHitUtils.Fields.PUSH_ID.getParamName() ) ) {
                SmartpushLog.d( Utils.TAG, "App opened from push sent by SMARTPUSH" );
                SmartpushService.startActionTrackAction( context,
                        SmartpushHitUtils.getValueFromPayload( SmartpushHitUtils.Fields.PUSH_ID, bundle ),
                        "MAIN", null, SmartpushHitUtils.Action.CLICKED.name(), null );
            }
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

    public static void subscribe( final Context context, String yourGooglePlayServiceProjectId ) {
        subscribe( context );
    }

    public static void subscribe( final Context context ) {
        if ( Smartpush.checkPlayServices( context ) ) {
            if ( checkSmartpush( context ) ) {
                SmartpushService.subscrive( context );
                SmartpushService.checkMsisdn( context );
                SmartpushService.checkCarriersName( context );
            }
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK.
     */
    public static boolean checkPlayServices( Context _c ) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable( _c );
        return ( resultCode == ConnectionResult.SUCCESS );
    }

    private static boolean checkSmartpush(Context context) {
        SmartpushLog.d( Utils.TAG, "checkSmartpush() : begin - Configurations tests : " + context.getPackageName() );

        if ( Utils.Smartpush.getMetadata( context, Utils.Constants.SMARTP_API_KEY) == null ) {
            throw new SmartpushException(
                    "Metadata not found! Add \"" + Utils.Constants.SMARTP_API_KEY + "\" to your manifest file!" );
        }

        if ( Utils.Smartpush.getMetadata( context, Utils.Constants.SMARTP_APP_ID) == null ) {
            throw new SmartpushException(
                    "Metadata not found! Add \"" + Utils.Constants.SMARTP_APP_ID + "\" to your manifest file!" );
        }

        SmartpushLog.d( Utils.TAG, "checkSmartpush() : Metadata, pass!" );

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

            SmartpushLog.d( Utils.TAG, "checkSmartpush() : Activity, pass!" );

        } catch ( PackageManager.NameNotFoundException e ) {
            // should never happen
            throw new RuntimeException( "Could not get package name: " + e );
        }

        SmartpushLog.d( Utils.TAG, "checkSmartpush() : end - Configurations tests : " + context.getPackageName() );

        return true;
    }

    private static boolean isRegistered( Context context ) {
        return  (  Utils.PreferenceUtils.readFromPreferences( context, Utils.Constants.SMARTP_REGID ) != null
                && Utils.PreferenceUtils.readFromPreferences( context, Utils.Constants.SMARTP_ALIAS ) != null
                && Utils.PreferenceUtils.readFromPreferences( context, Utils.Constants.SMARTP_HWID  ) != null );
    }
}
