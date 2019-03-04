package br.com.smartpush;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabio.licks on 09/02/16.
 */
final class GeoLocationDAO {
    public static final String TABLENAME = "LOCATION";

    public static int deleteAll( SQLiteDatabase db ) {
        return db.delete( TABLENAME, null, null );
    }

    public static int save( SQLiteDatabase db, GeoLocation data ) {
        if ( data != null ) {
            db.insert( TABLENAME, null, getContentValue( data ) );
            return 1;
        }
        return 0;
    }

    public static List<GeoLocation> listAll(SQLiteDatabase db ) {
        ArrayList<GeoLocation> list = new ArrayList<>();
        Cursor cursor = db.query(
                TABLENAME, new String[]{
                        GeoLocation.LAT,
                        GeoLocation.LNG,
                        GeoLocation.TIME }, null, null, null, null, null );

        while ( cursor != null && cursor.moveToNext() ) {
            list.add( new GeoLocation( cursor ) );
        }

        return list;
    }

    private static ContentValues getContentValue( GeoLocation location ) {
        ContentValues row = new ContentValues();
        row.put( GeoLocation.LAT, location.lat );
        row.put( GeoLocation.LNG, location.lng );
        row.put( GeoLocation.TIME, location.time );

        return row;
    }
}
