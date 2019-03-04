package br.com.smartpush;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static br.com.smartpush.Smartpush.EXTRA_VALUE;

class ActionTagManager {

    // TAGs
    public static final String ACTION_SET_TAG = "action.SET_TAG";
    public static final String ACTION_GET_TAG = "action.GET_TAG";

    // PARAMS
    public static final String EXTRA_KEY    = "extra.KEY";
    public static final String EXTRA_TYPE   = "extra.KEY_TYPE";
    public static final String EXTRA_METHOD_DEL = "extra.METHOD_DEL";

    /**
     * Starts this service to perform action setTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSetTag( Context context, String key, Boolean value ) {
        // It does not send empty boolean...
        if ( value == null ) return;

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_SET_TAG) ;
        intent.putExtra( EXTRA_KEY, key );
        intent.putExtra( EXTRA_TYPE, "BOOLEAN" );
        intent.putExtra(EXTRA_VALUE, value.toString());
        context.startService( intent );
    }

    /**
     * Starts this service to perform action setTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSetTag( Context context, String key, Double value ) {
        // It does not send empty double...
        if ( value == null ) return;

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_SET_TAG) ;
        intent.putExtra( EXTRA_KEY, key );
        intent.putExtra( EXTRA_TYPE, "NUMERIC" );
        intent.putExtra( EXTRA_VALUE, value.toString() );
        context.startService( intent );
    }

    /**
     * Starts this service to perform action setTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSetTag( Context context, String key, ArrayList<String> values ) {
        if ( values == null || values.size() == 0 ) return;

        String temp = ( new JSONArray( values ) ).toString();

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_SET_TAG) ;
        intent.putExtra( EXTRA_KEY, key );
        intent.putExtra( EXTRA_TYPE, "LIST" );
        intent.putExtra( EXTRA_VALUE, temp ) ;
        context.startService( intent );
    }

    public static Intent configActionSetTag(String key, @NonNull ArrayList<String> values ) {
        String temp = ( new JSONArray( values ) ).toString();

        Intent intent = new Intent( ) ;
        intent.setAction( ACTION_SET_TAG ) ;
        intent.putExtra( EXTRA_KEY, key );
        intent.putExtra( EXTRA_TYPE, "LIST" );
        intent.putExtra( EXTRA_VALUE, temp ) ;
        return intent;
    }

    /**
     * Starts this service to perform action setTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSetTag( Context context, String key, String value ) {
        // It does not send empty String...
        if ( value == null || "".equals( value.trim() ) ) return;

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_SET_TAG) ;
        intent.putExtra( EXTRA_KEY, key );
        intent.putExtra( EXTRA_TYPE, "STRING" );
        intent.putExtra( EXTRA_VALUE, value );
        context.startService(intent);
    }

    /**
     * Starts this service to perform action setTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSetTag( Context context, String key, Date value ) {
        String temp = (value != null) ? String.valueOf( value.getTime() / 1000 ) : null;

        // It does not send empty timestamp...
        if ( temp == null ) return;

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_SET_TAG) ;
        intent.putExtra( EXTRA_KEY, key );
        intent.putExtra( EXTRA_TYPE, "TIMESTAMP" );
        intent.putExtra(EXTRA_VALUE, temp);
        context.startService( intent );
    }

    /**
     * Starts this service to perform action delTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDelTagOrValue( Context context, String key, Boolean value ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_SET_TAG) ;
        intent.putExtra( EXTRA_KEY, key );
        intent.putExtra( EXTRA_TYPE, "BOOLEAN" );
        intent.putExtra( EXTRA_METHOD_DEL, true );

        if ( value != null )
            intent.putExtra( EXTRA_VALUE, value.toString() );
        context.startService( intent );
    }

    /**
     * Starts this service to perform action delTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDelTagOrValue( Context context, String key, Double value ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_SET_TAG) ;
        intent.putExtra( EXTRA_KEY, key );
        intent.putExtra( EXTRA_TYPE, "NUMERIC" );
        intent.putExtra( EXTRA_METHOD_DEL, true );

        if ( value != null )
            intent.putExtra( EXTRA_VALUE, value.toString() );
        context.startService( intent );
    }

    /**
     * Starts this service to perform action delTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDelTagOrValue( Context context, String key, ArrayList<String> values ) {
        String temp = (values == null || values.size() == 0) ? null : (new JSONArray(values)).toString();

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_SET_TAG) ;
        intent.putExtra( EXTRA_KEY, key );
        intent.putExtra( EXTRA_TYPE, "LIST" );
        intent.putExtra( EXTRA_METHOD_DEL, true );

        if ( temp != null )
            intent.putExtra( EXTRA_VALUE, temp ) ;
        context.startService( intent );
    }

    /**
     * Starts this service to perform action delTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDelTagOrValue( Context context, String key, String value ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_SET_TAG) ;
        intent.putExtra( EXTRA_KEY, key );
        intent.putExtra( EXTRA_TYPE, "STRING" );
        intent.putExtra( EXTRA_METHOD_DEL, true );

        if ( value != null )
            intent.putExtra( EXTRA_VALUE, value );
        context.startService( intent );
    }

    /**
     * Starts this service to perform action delTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDelTagOrValue( Context context, String key, Date value ) {
        String temp = (value != null) ? String.valueOf( value.getTime() / 1000 ) : null;
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_SET_TAG) ;
        intent.putExtra( EXTRA_KEY, key );
        intent.putExtra(EXTRA_TYPE, "TIMESTAMP");
        intent.putExtra( EXTRA_METHOD_DEL, true );

        if ( temp != null )
            intent.putExtra( EXTRA_VALUE, temp );
        context.startService( intent );
    }

    public static void startActionGetTagValues(Context context, String tag) {
        Intent intent = new Intent(context, SmartpushService.class);
        intent.setAction(ACTION_GET_TAG);
        intent.putExtra(EXTRA_KEY, tag);

        context.startService( intent );
    }

    /**
     * Handle action setTag in the provided background thread with the provided
     * parameters.
     */
    public static void handleActionSetOrDeleteTag( Context context, Intent data ) {
        HashMap<String, String> params = new HashMap<>();

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

        // tag info
        params.put( "key",
                data.getStringExtra( EXTRA_KEY ) );

        params.put( "type",
                data.getStringExtra( EXTRA_TYPE ) );

        if ( data.getBooleanExtra( EXTRA_METHOD_DEL, false ) ) {
            params.put( "_method", "DELETE");
        }

        if ( data.hasExtra( EXTRA_VALUE ) ) {
            params.put( "value", data.getStringExtra(EXTRA_VALUE));
        }

        boolean silent =
                ( data.getStringExtra( EXTRA_KEY ).equals( "__MSISDN__" )
                        || data.getStringExtra( EXTRA_KEY ).equals( "__CARRIER__" ) ) ? true : false;

        SmartpushHttpClient.post( "tag", params, context, silent );
    }

    /**
     * Handle action getTag in the provided background thread with the provided
     * parameters.
     */
    public static void handleActionGetTagValues( Context context, Intent data ){
        HashMap<String, String> params = new HashMap<>();

        String hwid =
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_HWID);

        params.put( "appid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_APP_ID ) );

        params.put( "devid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_API_KEY ) );

        params.put( "regid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_REGID) );

        // tag info
        params.put( "key",
                data.getStringExtra(EXTRA_KEY ) );

        LocalBroadcastManager
                .getInstance( context )
                .sendBroadcast(
                        new Intent( Smartpush.ACTION_GET_TAG_VALUES )
                                .putExtra( EXTRA_VALUE, SmartpushHttpClient
                                        .post( "tag/" + hwid, params, context, false ) ) );
    }
}