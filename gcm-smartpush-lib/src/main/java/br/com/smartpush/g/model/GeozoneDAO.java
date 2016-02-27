package br.com.smartpush.g.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by fabio.licks on 09/02/16.
 */
public class GeozoneDAO {

    public static final String TABLENAME = "GEOZONE";

    public static int deleteAll( SQLiteDatabase db ) {
        return db.delete( TABLENAME, null, null );
    }

    public static int saveAll( SQLiteDatabase db, List<Geozone> data ) {
        int rowNumber = 0;
        if ( data != null ) {
            Iterator<Geozone> it = data.iterator();
            while( it.hasNext() ) {
                db.insert( TABLENAME, null, getContentValue( it.next() ) );
                rowNumber++;
            }
        }

        return rowNumber;
    }

    public static List<Geozone> listAll( SQLiteDatabase db ) {
        ArrayList<Geozone> list = new ArrayList<>();
        Cursor cursor = db.query(
                TABLENAME, new String[]{ Geozone.ALIAS, Geozone.LAT, Geozone.LNG, Geozone.RADIUS }, null, null, null, null, null );

        while ( cursor != null && cursor.moveToNext() ) {
            list.add( new Geozone( cursor ) );
        }

        return list;
    }

    private static ContentValues getContentValue( Geozone geozone ) {
        ContentValues row = new ContentValues();
        row.put( Geozone.ALIAS, geozone.alias );
        row.put( Geozone.LAT, geozone.lat );
        row.put( Geozone.LNG, geozone.lng );
        row.put( Geozone.RADIUS, geozone.radius );

        return row;
    }

}