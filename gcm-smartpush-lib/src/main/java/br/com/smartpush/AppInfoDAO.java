package br.com.smartpush;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by t.licks on 23/08/17.
 */

class AppInfoDAO {
    public static final String TABLENAME = "APPSLIST";

    public static int deleteAll( SQLiteDatabase db ) {
        return db.delete( TABLENAME, null, null );
    }

    public static int save( SQLiteDatabase db, AppInfo data ) {
        if ( data != null ) {
            if ( data.getId() == 0 ) {
                db.insert( TABLENAME, null, getContentValue( data ) );
                // fetch ID
                data = findByPackageName( db, data.getPackageName() );
                return 1;
            } else {
                String query = "ID = ?";
                db.update( TABLENAME, getContentValue(data), query, new String[]{ String.valueOf( data.getId() ) } );
                return 1;
            }
        }
        return 0;
    }

    public static AppInfo findByPackageName( SQLiteDatabase db, String packageName ) {
        AppInfo appInfo = null;

        String query = AppInfo.PACKAGE_NAME + " = ?";
        Cursor cursor =
                db.query( TABLENAME, null, query, new String[] { packageName }, null, null, null );

        if ( cursor != null && cursor.moveToFirst() ) {
            appInfo = bindAppInfo( cursor );
        }

        return appInfo;
    }

    private static ContentValues getContentValue( AppInfo appInfo ) {
        ContentValues row = new ContentValues();
        row.put( AppInfo.PACKAGE_NAME, appInfo.getPackageName() );
        row.put( AppInfo.SINC_STATE, ( appInfo.isSinc() ? 1 : 0 ) );
        row.put( AppInfo.STATE, appInfo.getState() );

        return row;
    }

    private static AppInfo bindAppInfo( Cursor cursor ) {
        AppInfo item = new AppInfo();

        boolean sincState =
                ( ( cursor.getInt( cursor.getColumnIndex( AppInfo.SINC_STATE ) ) ) == 1 ) ? true : false;

        item.setId( cursor.getInt( cursor.getColumnIndex( "ID" ) ) );
        item.setPackageName( cursor.getString( cursor.getColumnIndex( AppInfo.PACKAGE_NAME ) ) );
        item.setSinc( sincState );
        item.setState( cursor.getInt( cursor.getColumnIndex( AppInfo.STATE ) ) );

        return item;
    }
}
