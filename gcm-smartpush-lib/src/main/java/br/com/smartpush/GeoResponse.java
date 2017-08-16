package br.com.smartpush;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by fabio.licks on 09/02/16.
 */
final class GeoResponse {

    public boolean status;

    public String message;

    public String hash;

    public ArrayList<Geozone> geozones;

    public GeoResponse( JSONObject o ) {

        try{
            if( o != null ) {
                status = o.getBoolean("status");
                message = o.getString("message");
                hash = o.getString( "hash" );

                if ( o.has( "geozones" ) ) {
                    geozones = new ArrayList<>();
                    JSONArray array = o.getJSONArray( "geozones" );
                    for ( int i = 0; i < array.length(); i++ ) {
                        geozones.add( new Geozone( array.getJSONObject( i ) ) );
                    }
                }
            }
        } catch( JSONException e ) {
            SmartpushLog.e( Utils.TAG, e.getMessage(), e );
        }
    }

    @Override
    public String toString() {
        return "{ \"status\":" + status +
                ", \"message\":\"" + message + "\"" +
                ", \"hash\":\"" + hash + "\"" +
                ( ( geozones != null ) ? ", \"geozones\":" + new Utils.ArrayUtils<Geozone>().toJsonArrayString( geozones ) : "" ) + "}";
    }
}
