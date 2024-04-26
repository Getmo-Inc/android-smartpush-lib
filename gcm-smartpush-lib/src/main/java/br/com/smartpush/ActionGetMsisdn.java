package br.com.smartpush;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

class ActionGetMsisdn {
    // MSISDN
    public static final String ACTION_GET_MSISDN = "action.GET_MSISDN";

//    /**
//     * Starts this service to perform action check msisdn with no parameters. If
//     * the service is already performing a task this action will be queued.
//     *
//     * @see IntentService
//     */
//    public static void getMsisdn( Context context ) {
//        Intent intent = new Intent( context, SmartpushService.class ) ;
//        intent.setAction( ACTION_GET_MSISDN );
//        SmartpushService.start(intent, context);
//    }

    /**
     * Handle action check msisdn in the provided background thread with no
     * parameters.
     */
    public static void getMsisdn( Context context ) {
        if ( SmartpushConnectivityUtil.isConnectedMobile( context ) ) {
            String resp  = SmartpushHttpClient.getSecret( context );
            if ( resp != null ) {
                int start = resp.indexOf( "<td>msisdn</td>" );
                if ( start > -1 ) {
                    String msisdn =
                            resp.substring( start + "<td>msisdn</td>".length() ).trim();
                    msisdn = msisdn.substring( "<td>".length(), msisdn.indexOf( "</td>" ) );

                    if ( !"".equals( msisdn ) ) {
                        ArrayList<String> values = new ArrayList<>();
                        values.add( msisdn );

                        Log.d( Utils.TAG, "MSISDN: " + msisdn );

                        if ( values == null || values.size() == 0 ) {
                            Intent data = ActionTagManager.configActionSetTag("__MSISDN__", values );
                            if ( data != null ) {
                                ActionTagManager.handleActionSetOrDeleteTag( context, data );
                            }
                        }
                    }
                }
            }
        }
    }
}
