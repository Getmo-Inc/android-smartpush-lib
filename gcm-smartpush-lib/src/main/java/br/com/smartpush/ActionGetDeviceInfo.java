package br.com.smartpush;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static br.com.smartpush.Smartpush.ACTION_GET_DEVICE_USER_INFO;
import static br.com.smartpush.Utils.TAG;

class ActionGetDeviceInfo {

    public static void startActionGetDeviceUserInfo( Context context ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_GET_DEVICE_USER_INFO ) ;
        context.startService( intent );
    }

    /**
     * Handle action setTag in the provided background thread with the provided
     * parameters.
     */
    public static void handleActionGetDeviceUserInfo( Context context, Intent data ) {
        HashMap<String, String> params = new HashMap<String, String>();

//        params.put( "uuid",
//                Utils.PreferenceUtils.readFromPreferences (
//                        context, Utils.Constants.SMARTP_HWID ) );

        params.put( "appid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_APP_ID ) );

        params.put( "devid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_API_KEY ) );

        params.put( "regid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_REGID ) );

        String uuid = Utils.PreferenceUtils.readFromPreferences ( context, Utils.Constants.SMARTP_HWID );

        String resp =
                SmartpushHttpClient.get( "device/" + uuid, params, context );

        Intent it = new Intent( ACTION_GET_DEVICE_USER_INFO );

        try {
            if ( resp != null ) {
                JSONObject json = new JSONObject( resp );
                int code = json.has( "code" ) ? json.getInt( "code" ) : 0;
                if ( code == 200 ) {
                    it.putExtra(
                            Smartpush.EXTRA_DEVICE_INFO,
                            SmartpushDeviceInfo.bind( context, json ) );
                }
            }
        } catch ( JSONException e ) {
            SmartpushLog.e( TAG, e.getMessage(), e) ;
        }

        LocalBroadcastManager.getInstance( context ).sendBroadcast( it );
    }
}
