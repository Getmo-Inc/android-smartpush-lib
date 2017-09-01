package br.com.smartpush;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import static br.com.smartpush.Utils.TAG;


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

    public static AppInfo findById( SQLiteDatabase db, int id ) {
        AppInfo appInfo = null;

        String query = AppInfo.ID + " = ?";
        Cursor cursor =
                db.query( TABLENAME, null, query, new String[] { String.valueOf( id ) }, null, null, null );

        if ( cursor != null && cursor.moveToFirst() ) {
            appInfo = bindAppInfo( cursor );
        }

        return appInfo;
    }

    public static List<AppInfo> listAll(SQLiteDatabase db ) {
        List<AppInfo> list = new ArrayList<>( );

        Cursor cursor = db.query( TABLENAME, null, null, null, null, null, null );

        if ( cursor != null ) {
            while( cursor.moveToNext() ) {
                AppInfo appInfo = bindAppInfo( cursor );
                SmartpushLog.d( TAG, appInfo.toString() );
                list.add( appInfo );
            }
        }

        return list;
    }

    public static List<String> listAllPackageNameByStatus( SQLiteDatabase db, int state, boolean sincState ) {
        List<String> list = new ArrayList<>( );

        String query = AppInfo.STATE + " = ? AND " + AppInfo.SINC_STATE + " = ?";
        Cursor cursor =
                db.query( TABLENAME, null, query, new String[]{ String.valueOf( state ), ( ( sincState ) ? "1" : "0" ) }, null, null, null );

        if ( cursor != null ) {
            while( cursor.moveToNext() ) {
                String packageName = cursor.getString( cursor.getColumnIndex( AppInfo.PACKAGE_NAME ) );
                SmartpushLog.d( TAG, packageName );
                list.add( packageName );
            }
        }

        return list;
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

        item.setId( cursor.getInt( cursor.getColumnIndex( AppInfo.ID ) ) );
        item.setPackageName( cursor.getString( cursor.getColumnIndex( AppInfo.PACKAGE_NAME ) ) );
        item.setSinc( sincState );
        item.setState( cursor.getInt( cursor.getColumnIndex( AppInfo.STATE ) ) );

        return item;
    }
}
