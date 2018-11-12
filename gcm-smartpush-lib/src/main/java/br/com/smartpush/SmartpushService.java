package br.com.smartpush;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static br.com.smartpush.Utils.Constants.NOTIF_PACKAGENAME;
import static br.com.smartpush.Utils.Constants.NOTIF_URL;
import static br.com.smartpush.Utils.Constants.PUSH_INTERNAL_ID;
import static br.com.smartpush.Utils.Constants.SMARTP_LOCATION_HASH;
import static br.com.smartpush.Utils.TAG;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */

public class SmartpushService extends IntentService {

    // Smartpush PROJECT ID
    private static final String PLAY_SERVICE_INTERNAL_PROJECT_ID = "520757792663";

    public static final int SERVICE_ID = 456123;

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_REGISTRATION = "action.REGISTRATION";
    private static final String ACTION_SET_TAG = "action.SET_TAG";
    private static final String ACTION_BLOCK_PUSH = "action.BLOCK_PUSH";
    private static final String ACTION_NEARESTZONE = "action.NEARESTZONE";
    private static final String ACTION_TRACK_ACTION = "action.TRACK_ACTION";
    private static final String ACTION_GET_MSISDN = "action.GET_MSISDN";
//    private static final String ACTION_GET_CARRIER = "action.GET_CARRIER";
//    public  static final String ACTION_GET_APP_LIST = "action.GET_APP_LIST";
    public  static final String ACTION_NOTIF_UPDATABLE = "action.UPDATABLE";
    public  static final String ACTION_NOTIF_UPDATABLE_NEXT = "action.UPDATABLE_NEXT";
    public  static final String ACTION_NOTIF_UPDATABLE_PREV = "action.UPDATABLE_PREV";
    public  static final String ACTION_NOTIF_CANCEL = "action.CANCEL";
    public  static final String ACTION_NOTIF_REDIRECT = "action.REDIRECT";

    public static final String ACTION_REGISTRATION_RESULT = "action.REGISTRATION_RESULT";
    public static final String ACTION_GET_DEVICE_USER_INFO = "action.GET_DEVICE_USER_INFO";

    private static final String EXTRA_KEY    = "extra.KEY";
    private static final String EXTRA_TYPE   = "extra.KEY_TYPE";
    private static final String EXTRA_VALUE  = "extra.VALUE";
    private static final String EXTRA_LAT    = "extra.LAT";
    private static final String EXTRA_LNG    = "extra.LNG";
    private static final String EXTRA_METHOD_DEL = "extra.METHOD_DEL";

    public SmartpushService() {
        super("SmartpushService");
    }

    /**
     * Starts this service to perform action susbcribe with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void subscrive( Context context ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_REGISTRATION);
        context.startService(intent);
    }

    private void createDefaultChannel() {

    }

    /**
     * Starts this service to perform action check msisdn with no parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    static void getMsisdn( Context context ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_GET_MSISDN );
        context.startService(intent);
    }

//    /**
//     * Starts this service to perform action retrieve a list of apps with no parameters. If
//     * the service is already performing a task this action will be queued.
//     *
//     * @see IntentService
//     */
//    static void getAppList( Context context ) {
//        Intent intent = new Intent( context, SmartpushService.class ) ;
//        intent.setAction(ACTION_GET_APP_LIST);
//        context.startService(intent);
//    }

//    /**
//     * Starts this service to perform action check msisdn with no parameters. If
//     * the service is already performing a task this action will be queued.
//     *
//     * @see IntentService
//     */
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
//    static void getMccMnc(Context context ) {
//        ArrayList<String> values = new ArrayList<>();
//        boolean supportMultiSim =
//                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
//
//        boolean hasPermissions  =
//                Utils.DeviceUtils.hasPermissions(
//                        context, android.Manifest.permission.READ_PHONE_STATE );
//
//        if ( supportMultiSim && hasPermissions ) {
//
//            //new way - gives access to all SIMs
//            SubscriptionManager subscriptionManager =
//                    ( SubscriptionManager ) context.getSystemService(
//                            Context.TELEPHONY_SUBSCRIPTION_SERVICE );
//
//            List<SubscriptionInfo> subInfoList =
//                    subscriptionManager.getActiveSubscriptionInfoList();
//
//            for( SubscriptionInfo info : subInfoList ) {
//                int mcc = info.getMcc();
//                int mnc = info.getMnc();
//
//                values.add( String.valueOf( mcc ) + String.valueOf( mnc ) );
//            }
//
//        } else {
//            TelephonyManager telephonyManager =
//                    ( TelephonyManager ) context.getSystemService( Context.TELEPHONY_SERVICE );
//
//            if ( telephonyManager != null ) {
//                String carrier = telephonyManager.getSimOperator();
//                if ( carrier != null
//                        && !"".equals( carrier.trim() )
//                        && !"NULL".equals( carrier.trim().toUpperCase() ) ) {
//
//                    values.add( carrier.toUpperCase() );
//                }
//            }
//        }
//
//        SmartpushLog.d( TAG, new JSONArray( values ).toString() );
//        startActionSetTag( context, "__CARRIER__", values );
//    }

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

    /**
     * Starts this service to perform action setTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionBlockPush( Context context, Boolean status ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_BLOCK_PUSH) ;
        intent.putExtra(EXTRA_VALUE, status);
        context.startService( intent );
    }


    public static void startActionGetDeviceUserInfo( Context context ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_GET_DEVICE_USER_INFO) ;
        context.startService( intent );
    }

    public static void startActionNearestZone( Context context, double lat, double lng ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_NEARESTZONE) ;
        intent.putExtra(EXTRA_LAT, lat);
        intent.putExtra(EXTRA_LNG, lng);
        context.startService( intent );
    }

    public static void startActionTrackAction( Context context, String pushId, String screenName, String category, String action, String label  ) {
        if ( !( ( pushId != null ) || ( screenName != null && action != null ) ) ) {
            //
            return;
        }

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_TRACK_ACTION) ;

        if ( pushId != null )
            intent.putExtra(
                    SmartpushHitUtils.Fields.PUSH_ID.getParamName(), pushId );

        if ( screenName != null )
            intent.putExtra(
                    SmartpushHitUtils.Fields.SCREEN_NAME.getParamName(), screenName );

        if ( category != null )
            intent.putExtra(
                    SmartpushHitUtils.Fields.CATEGORY.getParamName(), category );

        if ( action != null )
            intent.putExtra(
                    SmartpushHitUtils.Fields.ACTION.getParamName(), action );

        if ( label != null )
            intent.putExtra(
                    SmartpushHitUtils.Fields.LABEL.getParamName(), label );

        context.startService( intent );
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        if ( intent != null ) {
            final String action = intent.getAction();
            if ( ACTION_REGISTRATION.equals( action ) ) {
                handleActionSubscribe( );
            } else if ( ACTION_SET_TAG.equals( action ) ) {
                handleActionSetOrDeleteTag( intent );
            } else if ( ACTION_BLOCK_PUSH.equals( action ) ) {
                handleActionBlockPush( intent );
            } else if ( ACTION_GET_DEVICE_USER_INFO.equals( action ) ) {
                handleActionGetDeviceUserInfo( intent );
            } else if ( ACTION_NEARESTZONE.equals( action ) ) {
                handleActionNearestZone(intent);
            } else if ( ACTION_TRACK_ACTION.equals( action ) ) {
                handleActionTrackAction(intent);
            } else if ( ACTION_GET_MSISDN.equals( action ) ) {
                handleActionCheckMsisdn( );
            } else if ( ACTION_NOTIF_UPDATABLE.equals( action )
                    || ACTION_NOTIF_UPDATABLE_NEXT.equals( action )
                    || ACTION_NOTIF_UPDATABLE_PREV.equals( action ) ) {
                Bundle data = intent.getExtras();
                SmartpushLog.d( Utils.TAG, "----------> " + Utils.ArrayUtils.bundle2string( data ) );
                new SmartpushNotificationManager( this ).onMessageReceived( null, data );
            } else if ( ACTION_NOTIF_CANCEL.equals( action ) ) {
                Bundle data = intent.getExtras();
                handleActionCancelNotification( data );
            } else if ( ACTION_NOTIF_REDIRECT.equals( action ) ) {
                Bundle data = intent.getExtras();

                if ( data != null && SmartpushNotificationManager.isAutoCancel( data ) ) {
                    NotificationManager manager =
                            ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );

                    // Cancels the notification
                    manager.cancel( PUSH_INTERNAL_ID );
                }

                // Close notification bar!
                sendBroadcast( new Intent( Intent.ACTION_CLOSE_SYSTEM_DIALOGS ) );

                handleActionRedirectNotification( data );
            }
//            else if ( ACTION_GET_APP_LIST.equals( action ) ) {
//                handleActionSaveAppsListState();
//            }
        }
    }

    /**
     * Handle action Subscribe in the provided background thread with no
     * parameters.
     */
    private void handleActionSubscribe( ) {
        SmartpushDeviceInfo result = null;

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance( this );
            String token = instanceID.getToken(
                    PLAY_SERVICE_INTERNAL_PROJECT_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null );
            // [END get_token]
            SmartpushLog.d( TAG, "GCM Registration Token: " + token );

            result = sendRegistrationToServer( token );

            // [END register_for_gcm]
        } catch ( Exception e ) {
            SmartpushLog.e( TAG, "Failed to complete token refresh - " + e.getMessage(), e );
        }

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(ACTION_REGISTRATION_RESULT);

        if ( result != null ) {
            registrationComplete.putExtra(SmartpushDeviceInfo.EXTRA_DEVICE_INFO, result);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void handleActionRedirectNotification( Bundle data ) {

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
        String action = ( data.containsKey( NOTIF_URL ) )
                ? data.getString( NOTIF_URL ) : data.getString( "link" );

        String packageName = data.getString( NOTIF_PACKAGENAME );

        Intent intent =
                Utils.Smartpush.getIntentToRedirect( this, action, packageName, data );

        if ( intent != null && intent.resolveActivity( getPackageManager()) != null ) {
            startActivity( intent );
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

//        startActionTrackAction( this, pushId, null, null, SmartpushHitUtils.Action.CLICKED.name(), label );
        // TODO workaround android Oreo, pre-Black Friday 2018 ... repensar isso!!
        sendToAnalytics( this, pushId,  SmartpushHitUtils.Action.CLICKED.name() );

        SmartpushLog.d( TAG,
                "-------------------> APP OPENED FROM NOTIFICATION. - " + pushId );
    }

    private void handleActionCancelNotification( Bundle extras ) {
        // Hit notification canceled!
        String pushId =
                SmartpushHitUtils.getValueFromPayload(
                        SmartpushHitUtils.Fields.PUSH_ID, extras );

        startActionTrackAction( this, pushId, null, null, SmartpushHitUtils.Action.REJECTED.name(), null  );
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

    /**
     * Handle action check msisdn in the provided background thread with no
     * parameters.
     */
    private void handleActionCheckMsisdn( ) {
        if ( SmartpushConnectivityUtil.isConnectedMobile( getApplicationContext() ) ) {
            String resp  = SmartpushHttpClient.getSecret( this );
            if ( resp != null ) {
                int start = resp.indexOf("<td>msisdn</td>");
                if (start > -1) {
                    String tempString = resp.substring(start + "<td>msisdn</td>".length()).trim();
                    tempString = tempString.substring("<td>".length(), tempString.indexOf("</td>"));

                    if ( !"".equals( tempString ) ) {
                        ArrayList<String> values = new ArrayList<>();
                        values.add(tempString);
                        startActionSetTag(this, "__MSISDN__", values);
                    }
                }
            }
        }
    }

    /**
     * Handle action setTag in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSetOrDeleteTag( Intent data ) {
        HashMap<String, String> params = new HashMap<>();

        params.put( "uuid",  Utils.PreferenceUtils.readFromPreferences( this, Utils.Constants.SMARTP_HWID ) );
        params.put( "appid", Utils.Smartpush.getMetadata( this, Utils.Constants.SMARTP_APP_ID ) );
        params.put( "devid", Utils.Smartpush.getMetadata( this, Utils.Constants.SMARTP_API_KEY ) );
        params.put( "regid", Utils.PreferenceUtils.readFromPreferences( this, Utils.Constants.SMARTP_REGID ) );

        // tag info
        params.put( "key", data.getStringExtra( EXTRA_KEY ) );
        params.put( "type", data.getStringExtra( EXTRA_TYPE ) );

        if ( data.getBooleanExtra( EXTRA_METHOD_DEL, false ) ) {
            // Remove TAG Value
            params.put( "_method", "DELETE");
        }

        if ( data.hasExtra( EXTRA_VALUE ) ) {
            // Add value
            params.put("value", data.getStringExtra(EXTRA_VALUE));
        }

        boolean silent =
                ( data.getStringExtra( EXTRA_KEY ).equals( "__MSISDN__" )
                    || data.getStringExtra( EXTRA_KEY ).equals( "__CARRIER__" ) ) ? true : false;

        SmartpushHttpClient.post( "tag", params, this, silent );
    }

    /**
     * Handle action setTag in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBlockPush( Intent data ) {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put( "uuid",  Utils.PreferenceUtils.readFromPreferences(this, Utils.Constants.SMARTP_HWID) );
        params.put( "appid", Utils.Smartpush.getMetadata( this, Utils.Constants.SMARTP_APP_ID ) );
        params.put( "devid", Utils.Smartpush.getMetadata( this, Utils.Constants.SMARTP_API_KEY ) );
        params.put( "regid", Utils.PreferenceUtils.readFromPreferences( this, Utils.Constants.SMARTP_REGID ) );

        // tag info
        params.put( "_method", "PUT" );
        params.put( "block", data.getBooleanExtra( EXTRA_VALUE, false ) ? "1" : "0" );

        SmartpushHttpClient.post("device/optout", params, this, false);
    }

    /**
     * Handle action setTag in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetDeviceUserInfo( Intent data ) {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put( "uuid",  Utils.PreferenceUtils.readFromPreferences ( this, Utils.Constants.SMARTP_HWID ) );
        params.put( "appid", Utils.Smartpush.getMetadata( this, Utils.Constants.SMARTP_APP_ID ) );
        params.put( "devid", Utils.Smartpush.getMetadata( this, Utils.Constants.SMARTP_API_KEY ) );
        params.put( "regid", Utils.PreferenceUtils.readFromPreferences( this, Utils.Constants.SMARTP_REGID ) );

        String resp  = SmartpushHttpClient.get( "device", params, this );

        Intent it = new Intent(ACTION_GET_DEVICE_USER_INFO);
        try {
            JSONObject json = new JSONObject( resp );
            int code = json.has( "code" ) ? json.getInt( "code" ) : 0;
            if ( code == 200 ) {
                SmartpushDeviceInfo info = new SmartpushDeviceInfo( "" );
                info.alias  = json.getString( "alias" );
                info.regId  = json.getString( "regid" );
                info.optout = json.getString( "optout" );

                it.putExtra( SmartpushDeviceInfo.EXTRA_DEVICE_INFO, info );
            }

        } catch ( JSONException e ) {
            SmartpushLog.e( TAG, e.getMessage(), e) ;
        }

        LocalBroadcastManager.getInstance( this ).sendBroadcast(it);
    }

    private void handleActionNearestZone( Intent data ) {
        if ( !SmartpushHttpClient.isConnected(this) ) {
            return;
        }

        double lat = data.getDoubleExtra( EXTRA_LAT, 0 );
        double lng = data.getDoubleExtra( EXTRA_LNG, 0) ;
        boolean firstPoint = false;

        // Current location...
        GeoLocation currentLocation = new GeoLocation( lat, lng );

        SmartpushLog.d( TAG, "geo : [current] : " + currentLocation.toString() );

        // Obtem acesso ao banco de dados
        SQLiteDatabase db = new DatabaseManager( this ).getWritableDatabase();

        // Recupera a localização salva no último envio...
        ArrayList<GeoLocation> locations = (ArrayList<GeoLocation>) GeoLocationDAO.listAll( db );
        if ( locations.size() == 0 ) {
            GeoLocationDAO.save( db, currentLocation );
            firstPoint = true;
        }

        // Verifica se atravessou alguma geofence...
        GeoOverpass overpassed =
                Geozone.overpassed(
                        currentLocation.getLat(),
                        currentLocation.getLng(),
                        (ArrayList<Geozone>) GeozoneDAO.listAll( db ) );

        boolean wantSend;

        if ( overpassed == null ) {
            SmartpushLog.d( TAG, "geo : [overpassed] : [false]" );

            // Não atravessou nenhuma geozone, então ...
            GeoLocation oldLocation = ( locations.size() > 0 ) ? locations.get( 0 ) : currentLocation;

            SmartpushLog.d( TAG, "geo : [old] : " + oldLocation.toString() );

            double distance =
                    Geozone.distance(
                            oldLocation.getLat(), oldLocation.getLng(),
                            currentLocation.getLat(), currentLocation.getLng(), "K" );

            // Testa se distancia do pto atual em relaçao ao ultimo ponto enviado é maior
            // que 1.000 mts, ou se deve enviar IMEDIATAMENTE para o backend do SMARTPUSH!
            wantSend = ( firstPoint || distance > 1.0 || Utils.Constants.SMARTP_LOCATIONUPDT_IMMEDIATELY
                           .equals( Utils.Smartpush.getMetadata(this, Utils.Constants.SMARTP_LOCATIONUPDT ) ) );

        } else {
            SmartpushLog.d( TAG, "geo : [overpassed] : [true]" );
            SmartpushLog.d( TAG, "geo : [overpassed] : " + overpassed.toString() );

            // Atravessou uma geofence, então envia imediatamente!
            wantSend = true;
        }

        if ( wantSend ) {
            // Cria a request!
            GeoRequest req =
                    new GeoRequest(
                            this, Utils.PreferenceUtils.readFromPreferences( this, SMARTP_LOCATION_HASH, "(null)" ) );

            // Inicializa a lista de pontos com apenas o último ponto lido
            locations.clear();
            locations.add( currentLocation );

            req.setLocations( locations );

            if ( overpassed != null ) {
                req.setOverpass( overpassed );
            }

            // Faz o Geocode Reverse do ponto lido para enviar p/ o SMARTPUSH
            try {
                Geocoder geocoder = new Geocoder( this, new Locale( "pt", "BR" ) );
                List<Address> addresses =
                        geocoder.getFromLocation(
                                currentLocation.getLat(), currentLocation.getLng(), 1 ) ;

                if ( addresses != null && addresses.size() > 0 ) {
                    Address geoReverse = addresses.get(0);
                    GeoAddress address =
                            new GeoAddress(
                                    geoReverse.getCountryCode(),
                                    geoReverse.getCountryName(),
                                    geoReverse.getAdminArea(),
                                    geoReverse.getLocality(),
                                    geoReverse.getSubLocality()
                            );

                    req.setInfo( address );
                }
            } catch ( Exception e ) {
                SmartpushLog.e( TAG, e.getMessage(), e );
            }

            // Atualiza o backend, e então o device!
            try {
                String response = SmartpushHttpClient.post( "geozones", req.toJSONString(), this, false );

                if ( response != null ) {
                    GeoResponse resp = new GeoResponse( new JSONObject( response ) );

                    // Exclui todas as localizações salvas no dispositivo!
                    GeoLocationDAO.deleteAll( db );

                    // Salva a nova última localização enviada ao backend
                    GeoLocationDAO.save( db, currentLocation );

                    Utils.PreferenceUtils.saveOnPreferences( this, SMARTP_LOCATION_HASH, resp.hash );

                    if ( resp.geozones != null ) {
                        // Exclui todas as geozones salvas no dispositivo!
                        GeozoneDAO.deleteAll( db );

                        // Salva as novas geozones
                        GeozoneDAO.saveAll( db, resp.geozones );
                    }
                }

            } catch ( Exception e ) {
                SmartpushLog.e( TAG, e.getMessage(), e );
            }

        }
        // Fecha o banco local.
        db.close();
    }

    /**
     * Persist registration to third-party servers.
     *
     * This method associate the user's GCM registration token with your Smartpush server-side account
     *
     * @param token The new token.
     */
    private SmartpushDeviceInfo sendRegistrationToServer( String token ) {

        if ( token == null || "".equals( token ) ) {
            SmartpushLog.d( TAG, "GCM Registration Token: Fail!" );
            return null;
        }

        SmartpushDeviceInfo deviceInfo = new SmartpushDeviceInfo( token );

        HashMap<String, String> params = new HashMap<>();

        params.put( "uuid",  Utils.PreferenceUtils.readFromPreferences ( this, Utils.Constants.SMARTP_HWID ) );
        params.put( "appid", Utils.Smartpush.getMetadata( this, Utils.Constants.SMARTP_APP_ID ) );
        params.put( "devid", Utils.Smartpush.getMetadata( this, Utils.Constants.SMARTP_API_KEY ) );
        params.put( "regid", token );

        // device info
        params.put( "device", Utils.DeviceUtils.getDeviceName());
        params.put( "manufacturer", Utils.DeviceUtils.getDeviceManufacturer());
//        params.put( "latlong", "0,0");
        params.put( "framework", Build.VERSION.RELEASE);
        params.put( "platformId", "ANDROID" );

        try {
            JSONObject device = new JSONObject( SmartpushHttpClient.post( "device", params, this, false ) );
            SmartpushLog.d( TAG, device.toString( 4 ) );

            if ( device.has( "alias" ) ) {
                deviceInfo.alias = device.getString( "alias" );
                Utils.PreferenceUtils.saveOnPreferences(
                                this, Utils.Constants.SMARTP_ALIAS, deviceInfo.alias );
            }

            if ( device.has( "hwid" ) ) {
                deviceInfo.hwId = device.getString( "hwid" );
                Utils.PreferenceUtils.saveOnPreferences(
                                this, Utils.Constants.SMARTP_HWID, deviceInfo.hwId );
            }

            Utils.PreferenceUtils.saveOnPreferences(
                            this, Utils.Constants.SMARTP_REGID, deviceInfo.regId );

        } catch( JSONException e ) {
            SmartpushLog.e( TAG, e.getMessage(), e );
        }

        return deviceInfo;
    }

    private void handleActionTrackAction( Intent data ) {
        HashMap<String,String> fields = new HashMap<>();
        fields.put( "uuid", Utils.PreferenceUtils.readFromPreferences( this, Utils.Constants.SMARTP_HWID ) );
        fields.put( "appid", Utils.Smartpush.getMetadata(this, Utils.Constants.SMARTP_APP_ID) );
        fields.put( "devid", Utils.Smartpush.getMetadata( this, Utils.Constants.SMARTP_API_KEY ) );
        fields.put( "regid", Utils.PreferenceUtils.readFromPreferences( this, Utils.Constants.SMARTP_REGID ) );
        fields.put( "framework", Build.VERSION.RELEASE );
        fields.put( "sdk_v", getString(R.string.smartp_version) );
        fields.put( "plataformId", "ANDROID" );

        Bundle bundle = data.getExtras();

        String pushId = "";

        if ( bundle != null && bundle.containsKey( SmartpushHitUtils.Fields.PUSH_ID.getParamName() ) ) {
            pushId = bundle.getString(SmartpushHitUtils.Fields.PUSH_ID.getParamName());

            fields.put(SmartpushHitUtils.Fields.PUSH_ID.getParamName(), pushId );
        }

        if ( bundle != null && bundle.containsKey( SmartpushHitUtils.Fields.SCREEN_NAME.getParamName() ) )
            fields.put( SmartpushHitUtils.Fields.SCREEN_NAME.getParamName(),
                    bundle.getString( SmartpushHitUtils.Fields.SCREEN_NAME.getParamName() ) );

        if ( bundle != null && bundle.containsKey( SmartpushHitUtils.Fields.CATEGORY.getParamName() ) )
            fields.put( SmartpushHitUtils.Fields.CATEGORY.getParamName(),
                    bundle.getString( SmartpushHitUtils.Fields.CATEGORY.getParamName() ) );

        String action = "";
        if ( bundle != null && bundle.containsKey( SmartpushHitUtils.Fields.ACTION.getParamName() ) ) {
            action = bundle.getString(SmartpushHitUtils.Fields.ACTION.getParamName());
            fields.put(SmartpushHitUtils.Fields.ACTION.getParamName(), action );
        }

        if ( bundle != null && bundle.containsKey( SmartpushHitUtils.Fields.LABEL.getParamName() ) )
            fields.put( SmartpushHitUtils.Fields.LABEL.getParamName(),
                    bundle.getString( SmartpushHitUtils.Fields.LABEL.getParamName() ) );

//        if ( SmartpushHitUtils.shouldISendHitsToGetmo( bundle ) ) {
//            SmartpushHttpClient.post("hit", fields, this, false);
//        }

        sendToAnalytics( this, pushId, action );
    }

    private static void sendToAnalytics( Context context, String pushId, String action ) {
        // SEND HIT TO GOOGLE ANALYTICS
        String urlUA =
                "https://www.google-analytics.com/collect?v=1&tid=UA-108900354-1" +
                        "&cid=" +
                        Utils.PreferenceUtils.readFromPreferences( context, Utils.Constants.SMARTP_REGID ) +
                        "&t=pageview" +
                        "&dp=%2Fhits%2Fmobile%2F" + pushId + "%2F" +
                        Utils.Smartpush.getMetadata(context, Utils.Constants.SMARTP_APP_ID) + "%2F" + action;


        SmartpushLog.d( Utils.TAG, urlUA );

        try {
            URL targetUrl = new URL(urlUA);
            HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection();
            int responseCode = conn.getResponseCode();
            SmartpushLog.d( Utils.TAG, "[" + responseCode + "]" );
            conn.disconnect();
        } catch ( Exception e ) {
            SmartpushLog.e( Utils.TAG, e.getMessage(), e );
        }
    }

    // TODO implementar LIST APPS na forma de broadcast!
//    private void handleActionSaveAppsListState() {
//        SQLiteDatabase db = new DatabaseManager( this ).getWritableDatabase();
//
//        // Active apps list
//        List<String> installedAppsList =
//                Utils.DeviceUtils.getInstalledApps( this );
//
//        // List with last state sinc to SMARTPUSH
//        List<AppInfo> savedList =
//                AppInfoDAO.listAll( db );
//
//        Log.d( TAG, "savedList: " + savedList.size() );
//        Log.d( TAG, "installedAppsList: " + installedAppsList.size() );
//
//        // insert/update packages installed state
//        for ( String packageName : installedAppsList ) {
//            boolean found = false;
//            for( AppInfo saved : savedList ) {
//                if ( packageName.equals( saved.getPackageName() ) ) {
//                    Log.d( TAG, "savedList.contains: " + packageName );
//                    if ( saved != null && saved.getState() == AppInfo.UNINSTALLED ) {
//                        saved.setState( AppInfo.INSTALLED );
//                        saved.setSinc( false );
//                        AppInfoDAO.save( db, saved );
//                    }
//
//                    found = true;
//                    saved.setMatch( found );
//                    break;
//                }
//            }
//
//            if ( !found ) {
//                Log.d( TAG, "savedList.not.contains: " + packageName );
//                AppInfo newApp = new AppInfo();
//                newApp.setPackageName( packageName );
//                newApp.setState( AppInfo.INSTALLED );
//                newApp.setSinc( false );
//                newApp.setMatch( true );
//
//                AppInfoDAO.save( db, newApp );
//            }
//        }
//
//        // mark packages were uninstalled
//        for ( AppInfo item : savedList ) {
//            if ( !item.isMatch() ) {
//                Log.d( TAG, "savedList.contains.uninstalled.app: " + item.getPackageName() );
//                item.setState( AppInfo.UNINSTALLED );
//                item.setSinc( false );
//
//                AppInfoDAO.save( db, item );
//            }
//        }
//
//        // renew list with last state
//        savedList = AppInfoDAO.listAll( db );
//
//        List<String> uninstalled = new ArrayList<>();
//        List<String> installed   = new ArrayList<>();
//
//        for ( AppInfo item : savedList ) {
//            if ( !item.isSinc() ) {
//                if ( item.getState() == AppInfo.INSTALLED ) {
//                    Log.d( TAG, "INSTALLED: " + item.toString() );
//                    installed.add( item.getPackageName() );
//                }
//
//                if ( item.getState() == AppInfo.UNINSTALLED ) {
//                    Log.d( TAG, "UNINSTALLED: " + item.toString() );
//                    uninstalled.add( item.getPackageName() );
//                }
//
//                // LIST APPS - revisar para proxima versao da SDK, completar a operacao de persistencia
////                item.setSinc( SmartpushConnectivityUtil.isConnected( this ) );
////                AppInfoDAO.save( db, item );
//            }
//        }
//
//        // Release
//        db.close();
//    }
}