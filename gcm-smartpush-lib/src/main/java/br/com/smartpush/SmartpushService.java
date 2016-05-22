package br.com.smartpush;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;
import com.jaunt.component.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import br.com.smartpush.g.model.Geozone;
import br.com.smartpush.g.model.GeozoneDAO;
import br.com.smartpush.g.model.Location;
import br.com.smartpush.g.model.LocationDAO;
import br.com.smartpush.g.model.OpenDBHelper;
import br.com.smartpush.g.model.Overpass;
import br.com.smartpush.g.rest.GeoRequest;
import br.com.smartpush.g.rest.GeoResponse;
import br.com.smartpush.u.SmartpushConnectivityUtil;
import br.com.smartpush.u.SmartpushHitUtils;
import br.com.smartpush.u.SmartpushHttpClient;
import br.com.smartpush.u.SmartpushUtils;

import static br.com.smartpush.u.SmartpushUtils.SMARTP_LOCATION_HASH;
import static br.com.smartpush.u.SmartpushUtils.TAG;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */

public class SmartpushService extends IntentService {

    // Smartpush PROJECT ID
    private static final String PLAY_SERVICE_INTERNAL_PROJECT_ID = "520757792663";

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SMARTP_REGISTRATION = "br.com.smartpush.action.REGISTRATION";
    private static final String ACTION_SMARTP_SET_TAG = "br.com.smartpush.action.SET_TAG";
    private static final String ACTION_SMARTP_BLOCK_PUSH = "br.com.smartpush.action.BLOCK_PUSH";
    private static final String ACTION_SMARTP_NEARESTZONE = "br.com.smartpush.action.NEARESTZONE";
    private static final String ACTION_SMARTP_TRACK_ACTION = "br.com.smartpush.action.TRACK_ACTION";
    private static final String ACTION_SMARTP_CHECK_MSISDN = "br.com.smartpush.action.CHECK_MSISDN";
    private static final String ACTION_SMARTP_GET_CARRIER_NAME = "br.com.smartpush.action.GET_CARRIER_NAME";

    public static final String ACTION_SMARTP_REGISTRATION_RESULT = "br.com.smartpush.action.REGISTRATION_RESULT";
    public static final String ACTION_SMARTP_GET_DEVICE_USER_INFO = "br.com.smartpush.action.GET_DEVICE_USER_INFO";

    // TODO: Rename parameters
    private static final String EXTRA_KEY    = "br.com.smartpush.extra.KEY";
    private static final String EXTRA_TYPE   = "br.com.smartpush.extra.KEY_TYPE";
    private static final String EXTRA_VALUE  = "br.com.smartpush.extra.VALUE";
    private static final String EXTRA_LAT    = "br.com.smartpush.extra.LAT";
    private static final String EXTRA_LNG    = "br.com.smartpush.extra.LNG";
    private static final String EXTRA_METHOD_DEL = "br.com.smartpush.extra.METHOD_DEL";

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
        intent.setAction(ACTION_SMARTP_REGISTRATION);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action check msisdn with no parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void checkMsisdn( Context context ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_SMARTP_CHECK_MSISDN);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action check msisdn with no parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void checkCarriersName( Context context ) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE );

        // Carriers normalization
        ArrayList<String> values = new ArrayList<>();
        if ( telephonyManager != null ) {
            String[] carriers = telephonyManager.getNetworkOperatorName().split( "," );

            for ( int i = 0; i < carriers.length; i++ ) {
                if ( !"NULL".equals( carriers[ i ].toUpperCase() ) ) {
                    values.add( carriers[ i ].toUpperCase() );
                }
            }
        }

        startActionSetTag( context, "__CARRIERS__", values );
    }

    /**
     * Starts this service to perform action setTAG with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSetTag( Context context, String key, Boolean value ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_SMARTP_SET_TAG ) ;
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
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_SMARTP_SET_TAG ) ;
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
        String temp = (values == null || values.size() == 0) ? null : (new JSONArray(values)).toString();

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_SMARTP_SET_TAG ) ;
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
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_SMARTP_SET_TAG ) ;
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
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_SMARTP_SET_TAG ) ;
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
        intent.setAction( ACTION_SMARTP_SET_TAG ) ;
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
        intent.setAction( ACTION_SMARTP_SET_TAG ) ;
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
        intent.setAction( ACTION_SMARTP_SET_TAG ) ;
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
        intent.setAction( ACTION_SMARTP_SET_TAG ) ;
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
        intent.setAction( ACTION_SMARTP_SET_TAG ) ;
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
        intent.setAction(ACTION_SMARTP_BLOCK_PUSH) ;
        intent.putExtra(EXTRA_VALUE, status);
        context.startService( intent );
    }


    public static void startActionGetDeviceUserInfo( Context context ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_SMARTP_GET_DEVICE_USER_INFO) ;
        context.startService( intent );
    }

    public static void startActionNearestZone( Context context, double lat, double lng ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_SMARTP_NEARESTZONE ) ;
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
        intent.setAction( ACTION_SMARTP_TRACK_ACTION ) ;

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
            if ( ACTION_SMARTP_REGISTRATION.equals( action ) ) {
                handleActionSubscribe( );
            } else if ( ACTION_SMARTP_SET_TAG.equals( action ) ) {
                handleActionSetOrDeleteTag( intent );
            } else if ( ACTION_SMARTP_BLOCK_PUSH.equals( action ) ) {
                handleActionBlockPush( intent );
            } else if ( ACTION_SMARTP_GET_DEVICE_USER_INFO.equals( action ) ) {
                handleActionGetDeviceUserInfo( intent );
            } else if ( ACTION_SMARTP_NEARESTZONE.equals( action ) ) {
                handleActionNearestZone(intent);
            } else if ( ACTION_SMARTP_TRACK_ACTION.equals( action ) ) {
                handleActionTrackAction(intent);
            } else if ( ACTION_SMARTP_CHECK_MSISDN.equals( action ) ) {
                handleActionCheckMsisdn( );
            }
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
            Log.i( TAG, "GCM Registration Token: " + token );

            result = sendRegistrationToServer( token );

            // [END register_for_gcm]
        } catch ( Exception e ) {
            Log.e( TAG, "Failed to complete token refresh - " + e.getMessage(), e );
        }

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent( ACTION_SMARTP_REGISTRATION_RESULT );

        if ( result != null ) {
            registrationComplete.putExtra(SmartpushDeviceInfo.EXTRA_DEVICE_INFO, result);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Handle action check msisdn in the provided background thread with no
     * parameters.
     */
    private void handleActionCheckMsisdn( ) {
        if ( SmartpushConnectivityUtil.isConnectedMobile( getApplicationContext() ) ) {
            try {
                UserAgent userAgent = new UserAgent();
                userAgent.visit( "http://wapgw.purebros.com/headers/" );
                Table table = userAgent.doc.getTable( "<table border=\"1\">" );

                //get row elements right of msisdn
                Elements elements = table.getRowRightOf( "msisdn" );
                ArrayList<String> values = new ArrayList<>();

                for( Element element : elements ) {
                    values.add( element.getText() );
                }

                if ( values.size() > 0 ) {
                    startActionSetTag( getApplicationContext(), "__MSISDN__", values );
                }
            } catch( JauntException e ){
//                Log.e( TAG, e.getMessage(), e );
            }
        }
    }

    /**
     * Handle action setTag in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSetOrDeleteTag( Intent data ) {
        HashMap<String, String> params = new HashMap<>();

        params.put( "uuid",  SmartpushUtils.readFromPreferences( this, SmartpushUtils.SMARTP_HWID ) );
        params.put( "appid", SmartpushUtils.getSmartPushMetadata( this, SmartpushUtils.SMARTP_APP_ID ) );
        params.put( "devid", SmartpushUtils.getSmartPushMetadata( this, SmartpushUtils.SMARTP_API_KEY ) );

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

        SmartpushHttpClient.post("tag", params, this);
    }

    /**
     * Handle action setTag in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBlockPush( Intent data ) {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put( "uuid",  SmartpushUtils.readFromPreferences(this, SmartpushUtils.SMARTP_HWID) );
        params.put( "appid", SmartpushUtils.getSmartPushMetadata( this, SmartpushUtils.SMARTP_APP_ID ) );
        params.put( "devid", SmartpushUtils.getSmartPushMetadata( this, SmartpushUtils.SMARTP_API_KEY ) );

        // tag info
        params.put( "_method", "PUT" );
        params.put( "block", data.getBooleanExtra( EXTRA_VALUE, false ) ? "1" : "0" );

        SmartpushHttpClient.post("device/optout", params, this);
    }

    /**
     * Handle action setTag in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetDeviceUserInfo( Intent data ) {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put( "uuid",  SmartpushUtils.readFromPreferences ( this, SmartpushUtils.SMARTP_HWID ) );
        params.put( "appid", SmartpushUtils.getSmartPushMetadata( this, SmartpushUtils.SMARTP_APP_ID ) );
        params.put( "devid", SmartpushUtils.getSmartPushMetadata( this, SmartpushUtils.SMARTP_API_KEY ) );

        String resp  = SmartpushHttpClient.get( "device", params, this );

        Intent it = new Intent( ACTION_SMARTP_GET_DEVICE_USER_INFO );
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
            Log.e( TAG, e.getMessage(), e) ;
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
        Location currentLocation = new Location( lat, lng );

        Log.d( TAG, "geo : [current] : " + currentLocation.toString() );

        // Obtem acesso ao banco de dados
        SQLiteDatabase db = new OpenDBHelper( this ).getWritableDatabase();

        // Recupera a localização salva no último envio...
        ArrayList<Location> locations = (ArrayList<Location>) LocationDAO.listAll( db );
        if ( locations.size() == 0 ) {
            LocationDAO.save( db, currentLocation );
            firstPoint = true;
        }

        // Verifica se atravessou alguma geofence...
        Overpass overpassed =
                Geozone.overpassed(
                        currentLocation.getLat(),
                        currentLocation.getLng(),
                        (ArrayList<Geozone>) GeozoneDAO.listAll( db ) );

        boolean wantSend;

        if ( overpassed == null ) {
            Log.d( TAG, "geo : [overpassed] : [false]" );

            // Não atravessou nenhuma geozone, então ...
            Location oldLocation = ( locations.size() > 0 ) ? locations.get( 0 ) : currentLocation;

            Log.d( TAG, "geo : [old] : " + oldLocation.toString() );

            double distance =
                    Geozone.distance(
                            oldLocation.getLat(), oldLocation.getLng(),
                            currentLocation.getLat(), currentLocation.getLng(), "K" );

            // Testa se distancia do pto atual em relaçao ao ultimo ponto enviado é maior
            // que 1.000 mts, ou se deve enviar IMEDIATAMENTE para o backend do SMARTPUSH!
            wantSend = ( firstPoint || distance > 1.0 || SmartpushUtils.SMARTP_LOCATIONUPDT_IMMEDIATELY
                           .equals( SmartpushUtils.getSmartPushMetadata(
                                   this, SmartpushUtils.SMARTP_LOCATIONUPDT ) ) );

        } else {
            Log.d( TAG, "geo : [overpassed] : [true]" );
            Log.d( TAG, "geo : [overpassed] : " + overpassed.toString() );

            // Atravessou uma geofence, então envia imediatamente!
            wantSend = true;
        }

        if ( wantSend ) {
            // Cria a request!
            GeoRequest req =
                    new GeoRequest(
                            this, SmartpushUtils.readFromPreferences( this, SMARTP_LOCATION_HASH, "(null)" ) );

            // Inicializa a lista de pontos com apenas o último ponto lido
            locations.clear();
            locations.add( currentLocation );

            req.setLocations( locations );

            if ( overpassed != null ) {
                req.setOverpass( overpassed );
            }

            // Faz o Geocode Reverse do ponto lido para enviar p/ o SMARTPUSH
            try {
                Geocoder geocoder = new Geocoder( this, Locale.getDefault() );
                List<Address> addresses =
                        geocoder.getFromLocation(
                                currentLocation.getLat(), currentLocation.getLng(), 1 ) ;

                if ( addresses != null && addresses.size() > 0 ) {
                    Address geoReverse = addresses.get(0);
                    br.com.smartpush.g.model.Address address =
                            new br.com.smartpush.g.model.Address(
                                    geoReverse.getCountryCode(),
                                    geoReverse.getCountryName(),
                                    geoReverse.getAdminArea(),
                                    geoReverse.getLocality(),
                                    geoReverse.getSubLocality()
                            );

                    req.setInfo( address );
                }
            } catch ( Exception e ) {
                Log.e( TAG, e.getMessage(), e );
            }

            // Atualiza o backend, e então o device!
            try {
                String response = SmartpushHttpClient.post( "geozones", req.toJSONString(), this );

                if ( response != null ) {
                    GeoResponse resp = new GeoResponse( new JSONObject( response ) );

                    // Exclui todas as localizações salvas no dispositivo!
                    LocationDAO.deleteAll( db );

                    // Salva a nova última localização enviada ao backend
                    LocationDAO.save( db, currentLocation );

                    SmartpushUtils.saveOnPreferences( this, SMARTP_LOCATION_HASH, resp.hash );

                    if ( resp.geozones != null ) {
                        // Exclui todas as geozones salvas no dispositivo!
                        GeozoneDAO.deleteAll( db );

                        // Salva as novas geozones
                        GeozoneDAO.saveAll( db, resp.geozones );
                    }
                }

            } catch ( Exception e ) {
                Log.e( TAG, e.getMessage(), e );
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
            Log.d( TAG, "GCM Registration Token: Fail!" );
            return null;
        }

        SmartpushDeviceInfo deviceInfo = new SmartpushDeviceInfo( token );

        if ( token.equals( SmartpushUtils.readFromPreferences( this, SmartpushUtils.SMARTP_REGID ) ) ) {
            // Já registrado, retorna com dados locais...
            deviceInfo.alias = SmartpushUtils.readFromPreferences( this, SmartpushUtils.SMARTP_ALIAS );
            deviceInfo.hwId  = SmartpushUtils.readFromPreferences( this, SmartpushUtils.SMARTP_HWID );
            return deviceInfo;
        } else {
            // Novo registro, ou atualizacao...
            SmartpushUtils.deleteFromPreferences( this, SmartpushUtils.SMARTP_REGID );
            SmartpushUtils.deleteFromPreferences( this, SmartpushUtils.SMARTP_ALIAS );
            SmartpushUtils.deleteFromPreferences( this, SmartpushUtils.SMARTP_HWID );
        }

        HashMap<String, String> params = new HashMap<>();

        params.put( "uuid", SmartpushUtils.readFromPreferences  ( this, SmartpushUtils.SMARTP_HWID ) );
        params.put( "appid", SmartpushUtils.getSmartPushMetadata( this, SmartpushUtils.SMARTP_APP_ID ) );
        params.put( "devid", SmartpushUtils.getSmartPushMetadata( this, SmartpushUtils.SMARTP_API_KEY ) );
        params.put( "regid", token );

        // device info
        params.put( "device", SmartpushUtils.getDeviceName());
        params.put( "manufacturer", SmartpushUtils.getDeviceManufacturer());
        params.put( "latlong", "0,0");
        params.put( "framework", Build.VERSION.RELEASE);
        params.put( "platformId", "ANDROID" );

        try {
            JSONObject device = new JSONObject( SmartpushHttpClient.post( "device", params, this ) );
            Log.d( SmartpushUtils.TAG, device.toString( 4 ) );

            if ( device.has( "alias" ) ) {
                deviceInfo.alias = device.getString( "alias" );
                SmartpushUtils
                        .saveOnPreferences(
                                this, SmartpushUtils.SMARTP_ALIAS, deviceInfo.alias );
            }

            if ( device.has( "hwid" ) ) {
                deviceInfo.hwId = device.getString( "hwid" );
                SmartpushUtils
                        .saveOnPreferences(
                                this, SmartpushUtils.SMARTP_HWID, deviceInfo.hwId );
            }

            SmartpushUtils
                    .saveOnPreferences(
                            this, SmartpushUtils.SMARTP_REGID, deviceInfo.regId );

        } catch( JSONException e ) {
            Log.e( SmartpushUtils.TAG, e.getMessage(), e );
        }

        return deviceInfo;
    }

    private void handleActionTrackAction( Intent data ) {
        // TODO implements
        HashMap<String,String> fields = new HashMap<>();
        fields.put( "uuid", SmartpushUtils.readFromPreferences( this, SmartpushUtils.SMARTP_HWID ) );
        fields.put( "latlong", "0,0" );
        fields.put( "appid", SmartpushUtils.getSmartPushMetadata(this, SmartpushUtils.SMARTP_APP_ID) );
        fields.put( "devid", SmartpushUtils.getSmartPushMetadata( this, SmartpushUtils.SMARTP_API_KEY ) );
        fields.put( "framework", Build.VERSION.RELEASE );
        fields.put( "sdk_v", getString(R.string.smartp_version) );
        fields.put( "plataformId", "ANDROID" );

        Bundle b = data.getExtras();

        if ( b != null && b.containsKey( SmartpushHitUtils.Fields.PUSH_ID.getParamName() ) )
            fields.put( SmartpushHitUtils.Fields.PUSH_ID.getParamName(),
                    b.getString( SmartpushHitUtils.Fields.PUSH_ID.getParamName() ) );

        if ( b != null && b.containsKey( SmartpushHitUtils.Fields.SCREEN_NAME.getParamName() ) )
            fields.put( SmartpushHitUtils.Fields.SCREEN_NAME.getParamName(),
                    b.getString( SmartpushHitUtils.Fields.SCREEN_NAME.getParamName() ) );

        if ( b != null && b.containsKey( SmartpushHitUtils.Fields.CATEGORY.getParamName() ) )
            fields.put( SmartpushHitUtils.Fields.CATEGORY.getParamName(),
                    b.getString( SmartpushHitUtils.Fields.CATEGORY.getParamName() ) );

        if ( b != null && b.containsKey( SmartpushHitUtils.Fields.ACTION.getParamName() ) )
            fields.put( SmartpushHitUtils.Fields.ACTION.getParamName(),
                    b.getString( SmartpushHitUtils.Fields.ACTION.getParamName() ) );

        if ( b != null && b.containsKey( SmartpushHitUtils.Fields.LABEL.getParamName() ) )
            fields.put( SmartpushHitUtils.Fields.LABEL.getParamName(),
                    b.getString( SmartpushHitUtils.Fields.LABEL.getParamName() ) );

        SmartpushHttpClient.post( "hit", fields, this );
    }
}