package br.com.smartpush;

import android.util.Log;


/**
 * Created by fabio.licks on 04/06/16.
 */
final class SmartpushLog {

    public static void e( String tag, String errMessage, Throwable expt ) {
//        if ( BuildConfig.DEBUG ) {
            Log.e( tag, errMessage, expt );
//        }
    }

    public static void d( String tag, String message ) {
//        if ( BuildConfig.DEBUG ) {
            Log.d( tag, message );
//        }
    }
}
