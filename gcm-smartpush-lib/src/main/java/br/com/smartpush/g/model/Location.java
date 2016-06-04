package br.com.smartpush.g.model;

import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

import br.com.smartpush.u.SmartpushLog;
import br.com.smartpush.u.SmartpushUtils;

/**
 * Created by fabio.licks on 09/02/16.
 */
public class Location {

    public static final String LAT   = "LAT";
    public static final String LNG   = "LNG";
    public static final String TIME  = "TIME";

    private double lat;
    private double lng;
    private long  time; // timezone UTC (0)

    public Location( double lat, double lng ) {
        TimeZone timeZone = TimeZone.getTimeZone( "UTC" );
        Calendar calendar = Calendar.getInstance( timeZone );
        time = calendar.getTime().getTime() / 1000;

        this.lat = lat;
        this.lng = lng;
    }

    public Location ( JSONObject o ) {
        try {
            if (o != null) {
                lat = o.getDouble( LAT.toLowerCase() );
                lng = o.getDouble( LNG.toLowerCase() );
                time = o.getLong( TIME.toLowerCase() );
            }
        } catch (JSONException e) {
            SmartpushLog.getInstance( null ).e( SmartpushUtils.TAG, e.getMessage(), e );
        }
    }

    public Location ( Cursor cursor ) {
        if ( cursor != null ) {
            lat = cursor.getDouble( cursor.getColumnIndex( LAT ) );
            lng = cursor.getDouble( cursor.getColumnIndex( LNG ) );
            time = cursor.getLong( cursor.getColumnIndex( TIME ) );
        }
    }

    @Override
    public String toString() {
        return "{ \"lat\":" + lat +
                ", \"lng\":" + lng +
                ", \"time\":" + time + '}';
    }

    public double getLat() {
        return lat;
    }

    public void setLat( double lat ) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng( double lng ) {
        this.lng = lng;
    }

    public long getTime() {
        return time;
    }

    public void setTime( long time ) {
        this.time = time;
    }
}
