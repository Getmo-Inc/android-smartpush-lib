package br.com.smartpush;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by fabio.licks on 10/02/16.
 */
public abstract class SmartpushListenerService extends GcmListenerService {


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived( String from, Bundle data ) {
        String[] types = { "BANNER", "SLIDER", "CARROUSSEL", "SUBSCRIBE_EMAIL", "SUBSCRIBE_PHONE" };

        // Do something cool here...
        if ( data != null ) {
            if ( data.containsKey( "type" ) ) {
                String pushType = data.getString( "type" );
                if ( Arrays.asList( types ).contains( pushType ) ) {
                    // fetch payload extra & put it on bundle...
//                    POST /notifications/extra

//                  Params
                    HashMap<String, String> params = new HashMap<>();

//                  devid: (required) string | "000000000000000"
                    String devId =
                            Utils.Smartpush.getMetadata( this, Utils.Constants.SMARTP_API_KEY );
                    params.put( "devid", devId );

//                  appid: (required) string | "000000000000000"
                    String appId =
                            Utils.Smartpush.getMetadata( this, Utils.Constants.SMARTP_APP_ID );
                    params.put( "appid", appId );

//                  pushid: (required) string | "00000000000000000000000000000000"
                    String pushId = SmartpushHitUtils
                            .getValueFromPayload( SmartpushHitUtils.Fields.PUSH_ID, data );
                    params.put( SmartpushHitUtils.Fields.PUSH_ID.getParamName(), pushId );

                }
            }
        }

        new SmartpushNotificationManager( this ).onMessageReceived( from, data );

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        // sendNotification(message);
        // [END_EXCLUDE]
    }

    protected abstract void handleMessage( Bundle data );

}

