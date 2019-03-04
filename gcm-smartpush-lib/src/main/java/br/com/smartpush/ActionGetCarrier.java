package br.com.smartpush;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_PHONE_STATE;
import static br.com.smartpush.Utils.TAG;

public class ActionGetCarrier {
    public static final String ACTION_GET_CARRIER = "action.GET_CARRIER";

    /**
     * Starts this service to perform action check msisdn with no parameters. If
     * the service is already performing a task this action will be queued.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    static void getMccMnc( Context context ) {
        ArrayList<String> values = new ArrayList<>();
        boolean supportMultiSim =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

        boolean hasPermissions  =
                Utils.DeviceUtils.hasPermissions( context, READ_PHONE_STATE );

        if ( supportMultiSim && hasPermissions ) {
            //new way - gives access to all SIMs
            SubscriptionManager subscriptionManager =
                    ( SubscriptionManager ) context.getSystemService(
                            Context.TELEPHONY_SUBSCRIPTION_SERVICE );

            @SuppressLint( "MissingPermission" )
            List<SubscriptionInfo> subInfoList =
                    subscriptionManager.getActiveSubscriptionInfoList();

            for( SubscriptionInfo info : subInfoList ) {
                int mcc = info.getMcc();
                int mnc = info.getMnc();

                values.add( String.valueOf( mcc ) + String.valueOf( mnc ) );
            }
        } else {
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE );

            if ( telephonyManager != null ) {
                String carrier = telephonyManager.getSimOperator();
                if ( carrier != null
                        && !"".equals( carrier.trim() )
                        && !"NULL".equals( carrier.trim().toUpperCase() ) ) {

                    values.add( carrier.toUpperCase() );
                }
            }
        }

        SmartpushLog.d( TAG, new JSONArray( values ).toString() );
        if ( values == null || values.size() == 0 ) {

            Log.d( Utils.TAG, "__CARRIER__:" + new Utils.ArrayUtils<String>().toJsonArrayString( values ) );

            Intent data = ActionTagManager.configActionSetTag("__CARRIER__", values );
            if ( data != null ) {
                ActionTagManager.handleActionSetOrDeleteTag( context, data );
            }
        }
    }
}
