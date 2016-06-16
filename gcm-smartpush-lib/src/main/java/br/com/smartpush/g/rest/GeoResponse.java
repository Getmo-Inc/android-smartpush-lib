package br.com.smartpush.g.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import br.com.smartpush.g.model.Geozone;
import br.com.smartpush.u.SmartpushArraysUtil;
import br.com.smartpush.u.SmartpushLog;
import br.com.smartpush.u.SmartpushUtils;

/**
 * Created by fabio.licks on 09/02/16.
 */
public class GeoResponse {

    public boolean status;

//    public int code;

    public String message;

    public String hash;

    public ArrayList<Geozone> geozones;

    public GeoResponse( JSONObject o ) {

        try{
            if( o != null ) {
                status = o.getBoolean("status");
//                code = o.getInt("code");
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
            SmartpushLog.getInstance( null ).e( SmartpushUtils.TAG, e.getMessage(), e );
        }
    }

    @Override
    public String toString() {
        return "{ \"status\":" + status +
//                ", \"code\":" + code +
                ", \"message\":\"" + message + "\"" +
                ", \"hash\":\"" + hash + "\"" +
                ( ( geozones != null ) ? ", \"geozones\":[" + new SmartpushArraysUtil<Geozone>().toString( geozones ) + "]" : "" ) + "}";
    }
}
