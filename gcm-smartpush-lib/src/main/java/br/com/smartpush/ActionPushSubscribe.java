package br.com.smartpush;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static br.com.smartpush.Utils.TAG;

class ActionPushSubscribe {

    public static final String ACTION_REGISTRATION = "action.REGISTRATION";

    public static void subscribe( Context context, String token ) {
        sendRegistrationToServer( context, token );
    }

    public static void subscribeByService( Context context, String token ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_REGISTRATION );
        intent.putExtra( ACTION_REGISTRATION, token );
        context.startService( intent );
    }

    private static void notify( Context context, SmartpushDeviceInfo deviceInfo ) {
        Intent registrationComplete = new Intent( Smartpush.ACTION_REGISTRATION_RESULT );

        if ( deviceInfo != null ) {
            registrationComplete
                    .putExtra(
                            Smartpush.EXTRA_DEVICE_INFO, deviceInfo );
        }

        LocalBroadcastManager.getInstance( context ).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     * This method associate the user's GCM registration token with your Smartpush server-side account
     *
     * @param token The new token.
     */
    public static void sendRegistrationToServer( Context context, String token ) {
        SmartpushDeviceInfo deviceInfo = new SmartpushDeviceInfo( token );

        HashMap<String, String> params = new HashMap<>();

        params.put( "uuid",  Utils.PreferenceUtils.readFromPreferences ( context, Utils.Constants.SMARTP_HWID ) );
        params.put( "appid", Utils.Smartpush.getMetadata( context, Utils.Constants.SMARTP_APP_ID ) );
        params.put( "devid", Utils.Smartpush.getMetadata( context, Utils.Constants.SMARTP_API_KEY ) );
        params.put( "regid", token );

        // device info
        params.put( "device", Utils.DeviceUtils.getDeviceName());
        params.put( "manufacturer", Utils.DeviceUtils.getDeviceManufacturer());
        params.put( "framework", Build.VERSION.RELEASE );
        params.put( "platformId", "ANDROID" );
        params.put( "sdk_version", context.getString( R.string.smartp_version ) );

        try {
            String response = SmartpushHttpClient.post( "device", params, context, false );
            if ( response != null ) {
                JSONObject device = new JSONObject(response);

                SmartpushLog.d(TAG, device.toString(4));

                if (device.has("alias")) {
                    deviceInfo.alias = device.getString("alias");
                    Utils.PreferenceUtils.saveOnPreferences(
                            context, Utils.Constants.SMARTP_ALIAS, deviceInfo.alias);
                }

                if (device.has("hwid")) {
                    deviceInfo.hwId = device.getString("hwid");
                    Utils.PreferenceUtils.saveOnPreferences(
                            context, Utils.Constants.SMARTP_HWID, deviceInfo.hwId);
                }

                Utils.PreferenceUtils.saveOnPreferences(
                        context, Utils.Constants.SMARTP_REGID, deviceInfo.regId);

                notify( context, deviceInfo );
            }
        } catch( JSONException e ) {
            SmartpushLog.e( TAG, e.getMessage(), e );
        }

        ActionGetMsisdn.getMsisdn( context );
        ActionGetCarrier.getMccMnc( context );
    }
}
