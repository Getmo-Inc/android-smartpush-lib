package br.com.smartpush.g.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by fabio.licks on 09/02/16.
 */
public class OpenDBHelper  extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "SMARTPUSH";

    private static final String[] CREATE_SCRIPTS = new String[] {
            "CREATE TABLE IF NOT EXISTS GEOZONE ( " +
                    " ID INTEGER PRIMARY KEY NOT NULL," +
                    " ALIAS TEXT NOT NULL," +
                    " LAT REAL NOT NULL," +
                    " LNG REAL NOT NULL," +
                    " RADIUS INTEGER NOT NULL );",

            "CREATE TABLE IF NOT EXISTS LOCATION ( " +
                    " ID INTEGER PRIMARY KEY NOT NULL," +
                    " LAT REAL NOT NULL," +
                    " LNG REAL NOT NULL," +
                    " TIME INTEGER NOT NULL );"
    };

    private static final String[] DROP_SCRIPTS = new String[] {
            "DROP TABLE IF EXISTS GEOZONE;",
            "DROP TABLE IF EXISTS LOCATION;"
    };

    public OpenDBHelper( Context context ) {
        super(context, DATABASE_NAME, null, VERSION );
    }

    @Override
    public void onCreate(
        SQLiteDatabase sqLiteDatabase ) {
        for ( int i = 0; i < CREATE_SCRIPTS.length; i++ ) {
            sqLiteDatabase.execSQL( CREATE_SCRIPTS[ i ] );
        }
    }

    @Override
    public void onUpgrade(
        SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion ) {
        for ( int i = 0; i < DROP_SCRIPTS.length; i++ ) {
            sqLiteDatabase.execSQL( DROP_SCRIPTS[ i ] );
        }

        onCreate( sqLiteDatabase );
    }
}