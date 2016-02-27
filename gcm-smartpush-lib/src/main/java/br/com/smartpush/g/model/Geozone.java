package br.com.smartpush.g.model;

import android.database.Cursor;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import br.com.smartpush.u.SmartpushUtils;

/**
 * Created by fabio.licks on 09/02/2016.
 */
public class Geozone {

    public static final String ALIAS  = "ALIAS";
    public static final String LAT    = "LAT";
    public static final String LNG    = "LNG";
    public static final String RADIUS = "RADIUS";

    public String alias;
    public double lat;
    public double lng;
    public int radius;

    public Geozone() {
        // default
    }

    public Geozone( JSONObject o ) {
        try {
            if ( o != null ) {
                alias = o.getString( ALIAS.toLowerCase() );
                lat = o.getDouble( LAT.toLowerCase() );
                lng = o.getDouble( LNG.toLowerCase() );
                radius = o.getInt( RADIUS.toLowerCase() );
            }
        } catch (JSONException e) {
            Log.e( SmartpushUtils.TAG, e.getMessage(), e );
        }

    }

    public Geozone( Cursor cursor ) {
        if ( cursor != null ) {
            alias = cursor.getString( cursor.getColumnIndex( ALIAS ) );
            lat = cursor.getDouble( cursor.getColumnIndex( LAT ) );
            lng = cursor.getDouble( cursor.getColumnIndex( LNG ) );
            radius = cursor.getInt( cursor.getColumnIndex( RADIUS ) );
        }

    }

    @Override
    public String toString() {
        return "{ \"alias\":\"" + alias + "\"" +
                ", \"lat\":" + lat +
                ", \"lng\":" + lng +
                ", \"radius\":" + radius + '}';
    }

    public static Overpass overpassed ( double lat, double lon, List<Geozone> geozones ) {
        Iterator<Geozone> iterator = ( geozones != null ) ? geozones.iterator() : null;

        while( iterator != null && iterator.hasNext() ) {
            Geozone geozone = iterator.next();

            double distance = distance( lat, lon, geozone.lat, geozone.lng, "K" );
            double radiusInKM = ( double )( geozone.radius / 1000.0 );
            if ( distance < radiusInKM ) {
                return new Overpass( geozone );
            }
        }

        return null;
    }

    public static double distance( double lat1, double lon1, double lat2, double lon2, String unit ) {
        double theta = lon1 - lon2;
        double dist = Math.sin( deg2rad( lat1 ) )
                * Math.sin( deg2rad( lat2 ) )
                + Math.cos( deg2rad( lat1 ) )
                * Math.cos( deg2rad( lat2 ) )
                * Math.cos( deg2rad( theta ) );

        dist = Math.acos( dist );
        dist = rad2deg( dist );
        dist = dist * 60 * 1.1515;
        if ( unit == "K" ) {
            dist = dist * 1.609344;
        } else if ( unit == "N" ) {
            dist = dist * 0.8684;
        }

        return Double.isNaN( dist ) ? 0 : dist;
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts decimal degrees to radians			:*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad( double deg ) {
        return ( deg * Math.PI / 180.0 );
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees			:*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg( double rad ) {
        return ( rad * 180 / Math.PI );
    }

}