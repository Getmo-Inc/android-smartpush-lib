package br.com.smartpush;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

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

