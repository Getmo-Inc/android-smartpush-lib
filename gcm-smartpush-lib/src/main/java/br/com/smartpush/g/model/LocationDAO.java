package br.com.smartpush.g.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabio.licks on 09/02/16.
 */
public class LocationDAO {
    public static final String TABLENAME = "LOCATION";

    public static int deleteAll( SQLiteDatabase db ) {
        return db.delete( TABLENAME, null, null );
    }

    public static int save( SQLiteDatabase db, Location data ) {
        if ( data != null ) {
            db.insert( TABLENAME, null, getContentValue( data ) );
            return 1;
        }
        return 0;
    }

    public static List<Location> listAll( SQLiteDatabase db ) {
        ArrayList<Location> list = new ArrayList<>();
        Cursor cursor = db.query(
                TABLENAME, new String[]{ Location.LAT, Location.LNG, Location.TIME }, null, null, null, null, null );

        while ( cursor != null && cursor.moveToNext() ) {
            list.add( new Location( cursor ) );
        }

        return list;
    }

    private static ContentValues getContentValue( Location location ) {
        ContentValues row = new ContentValues();
        row.put( Location.LAT, location.getLat() );
        row.put( Location.LNG, location.getLng() );
        row.put( Location.TIME, location.getTime() );

        return row;
    }
}
