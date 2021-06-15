package br.com.smartpush;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static br.com.smartpush.Utils.Constants.SMARTP_LOCATION_HASH;
import static br.com.smartpush.Utils.TAG;

class ActionNearestzone {
    // GEOZONE
    public static final String ACTION_NEARESTZONE = "action.NEARESTZONE";

    // PARAMS
    private static final String EXTRA_LAT    = "extra.LAT";
    private static final String EXTRA_LNG    = "extra.LNG";

    public static void startActionNearestZone( Context context, double lat, double lng ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction(ACTION_NEARESTZONE) ;
        intent.putExtra(EXTRA_LAT, lat);
        intent.putExtra(EXTRA_LNG, lng);
        context.startService( intent );
    }

    public static void handleActionNearestZone( Context context, Intent data ) {
        if ( !SmartpushHttpClient.isConnected( context ) ) {
            return;
        }

        double lat = data.getDoubleExtra( EXTRA_LAT, 0 );
        double lng = data.getDoubleExtra( EXTRA_LNG, 0) ;
        boolean firstPoint = false;

        // Current location...
        GeoLocation currentLocation = new GeoLocation( lat, lng );

        SmartpushLog.d( TAG, "geo : [current.position] : " + currentLocation.toString() );

        // Obtem acesso ao banco de dados
        SQLiteDatabase db =
                new DatabaseManager( context ).getWritableDatabase();

        // Recupera a localização salva no último envio...
        ArrayList<GeoLocation> locations = (ArrayList<GeoLocation>) GeoLocationDAO.listAll( db );
        if ( locations.size() == 0 ) {
            GeoLocationDAO.save( db, currentLocation );
            firstPoint = true;
        }

        // Verifica se atravessou alguma geofence...
        GeoOverpass overpassed =
                Geozone.overpassed(
                        currentLocation.lat, currentLocation.lng, GeozoneDAO.listAll( db ) );

        boolean wantSend;

        if ( overpassed == null ) {
            SmartpushLog.d( TAG, "geo : [overpassed] : [false]" );

            // Não atravessou nenhuma geozone, então ...
            GeoLocation oldLocation =
                    ( locations.size() > 0 ) ? locations.get( 0 ) : currentLocation;

            SmartpushLog.d( TAG, "geo : [old.position] : " + oldLocation.toString() );

            double distance =
                    Geozone.distance(
                            oldLocation.lat, oldLocation.lng, currentLocation.lat, currentLocation.lng, "K" );

            // Testa se distancia do pto atual em relaçao ao ultimo ponto enviado é maior
            // que 1.000 mts, ou se deve enviar IMEDIATAMENTE para o backend do SMARTPUSH!
            wantSend = ( firstPoint || distance > 1.0 || Utils.Constants.SMARTP_LOCATIONUPDT_IMMEDIATELY
                    .equals( Utils.Smartpush.getMetadata( context, Utils.Constants.SMARTP_LOCATIONUPDT ) ) );

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
                            context, Utils.PreferenceUtils.readFromPreferences(
                                    context, SMARTP_LOCATION_HASH, "(null)" ) );

            // Inicializa a lista de pontos com apenas o último ponto lido
            locations.clear();
            locations.add( currentLocation );

            req.setLocations( locations );

            if ( overpassed != null ) {
                req.setOverpass( overpassed );
            }

            // Faz o Geocode Reverse do ponto lido para enviar p/ o SMARTPUSH
            try {
                Geocoder geocoder = new Geocoder( context, new Locale( "pt", "BR" ) );
                List<Address> addresses =
                        geocoder.getFromLocation(
                                currentLocation.lat, currentLocation.lng, 1 ) ;

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
                String response =
                        SmartpushHttpClient.post( "geozones", req.toJSONString(), context, false );

                if ( response != null ) {
                    GeoResponse resp = new GeoResponse( new JSONObject( response ) );

                    // Exclui todas as localizações salvas no dispositivo!
                    GeoLocationDAO.deleteAll( db );

                    // Salva a nova última localização enviada ao backend
                    GeoLocationDAO.save( db, currentLocation );

                    Utils.PreferenceUtils.saveOnPreferences( context, SMARTP_LOCATION_HASH, resp.hash );

                    if ( resp.geozones != null ) {
                        // Exclui todas as geozones salvas no dispositivo!
                        GeozoneDAO.deleteAll( db );

                        // Salva as novas geozones
                        GeozoneDAO.saveAll( db, resp.geozones );

                        LocalBroadcastManager
                                .getInstance( context )
                                .sendBroadcast( new Intent( Smartpush.ACTION_GEOZONES_UPDATED ) );
                    }
                }

            } catch ( Exception e ) {
                SmartpushLog.e( TAG, e.getMessage(), e );
            }

        }
        // Fecha o banco local.
        db.close();
    }
}
