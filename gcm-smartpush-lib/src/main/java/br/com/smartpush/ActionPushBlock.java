package br.com.smartpush;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;

import static br.com.smartpush.Smartpush.EXTRA_VALUE;

class ActionPushBlock {

    // (UN)BLOCK PUSH
    public static final String ACTION_BLOCK_PUSH = "action.BLOCK_PUSH";

    /**
     * Starts this service to perform action setTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionBlockPush(Context context, Boolean status ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_BLOCK_PUSH) ;
        intent.putExtra(EXTRA_VALUE, status);
        context.startService( intent );
    }

    /**
     * Handle action setTag in the provided background thread with the provided
     * parameters.
     */
    public static void handleActionBlockPush( Context context, Intent data ) {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put( "uuid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_HWID ) );

        params.put( "appid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_APP_ID ) );

        params.put( "devid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_API_KEY ) );

        params.put( "regid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_REGID ) );

        params.put( "block",
                data.getBooleanExtra( EXTRA_VALUE, false ) ? "1" : "0" );

        params.put( "_method", "PUT" );

        SmartpushHttpClient.post( "device/optout", params, context, false );
    }



}
