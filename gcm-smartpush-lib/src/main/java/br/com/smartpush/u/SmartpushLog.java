package br.com.smartpush.u;

import android.content.Context;
import android.util.Log;

/**
 * Created by fabio.licks on 04/06/16.
 */
public class SmartpushLog {

    private static SmartpushLog _instance;
    private static boolean debug;

    private SmartpushLog() {

    }

    public static SmartpushLog getInstance( Context context ) {
        if ( _instance == null ) {
            _instance = new SmartpushLog();
            if ( context != null ) {
                debug = ("true".equals(SmartpushUtils.getSmartPushMetadata(context, SmartpushUtils.SMARTP_DEBUG)) ? true : false);
            }
        }

        return _instance;
    }

    public void e( String tag, String errMessage, Throwable expt ) {
        if ( debug ) {
            Log.e( tag, errMessage, expt );
        }
    }

    public void d( String tag, String message ) {
        if ( debug ) {
            Log.d( tag, message );
        }
    }

}
