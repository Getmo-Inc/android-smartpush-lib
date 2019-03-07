package br.com.getmo.inbox;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Notification {
    public String pushId;
    public boolean opened;
    public JSONObject payload;
    public JSONObject extras;
    public Date createdAt;
    public Date sentAt;

    public Notification( JSONObject obj ) {
        if ( obj == null ) return;

        try {
            pushId = obj.getString( "pushid" );
        } catch ( JSONException e ) {
            Log.e( "DEBUG", e.getMessage(), e );
        }

        try {
            int clicked = obj.getInt( "clicked" );
            opened = ( clicked != 0 );
        } catch ( JSONException e ) {
            Log.e( "DEBUG", e.getMessage(), e );
        }

        try {
            payload = obj.getJSONObject( "payload" );
        } catch ( JSONException e ) {
            Log.e( "DEBUG", e.getMessage(), e );
        }

        try {
            extras = obj.getJSONObject( "extra" );
        } catch ( JSONException e ) {
            Log.e( "DEBUG", e.getMessage(), e );
        }

        try {
            String keyValue = obj.getString( "created_at" );
            SimpleDateFormat formatter = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
            createdAt = formatter.parse( keyValue );
        } catch ( JSONException | ParseException e ) {
            Log.e( "DEBUG", e.getMessage(), e );
        }

        try {
            String keyValue = obj.getString( "sent_at" );
            SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
            sentAt = formatter.parse( keyValue );
        } catch ( JSONException | ParseException e ) {
            Log.e( "DEBUG", e.getMessage(), e );
        }
    }

    public String getPayloadValue( String key ) {
        try {
            return ( payload.has( key ) ) ? payload.getString( key ) : null;
        } catch ( JSONException e ) {
            Log.e( "DEBUG", e.getMessage(), e );
        }
        return null;
    }

    public String getExtrasValue( String key ) {
        try {
            return ( extras.has( key ) ) ? extras.getString( key ) : null;
        } catch ( JSONException e ) {
            Log.e( "DEBUG", e.getMessage(), e );
        }

        return null;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "pushId='" + pushId + '\'' +
                ", opened=" + opened +
                ", payload=" + payload +
                ", extras=" + extras +
                ", createdAt=" + createdAt +
                ", sentAt=" + sentAt +
                '}';
    }
}
