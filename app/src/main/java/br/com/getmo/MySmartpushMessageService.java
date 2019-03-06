package br.com.getmo;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import br.com.smartpush.SmartpushMessagingListenerService;

public class MySmartpushMessageService extends SmartpushMessagingListenerService {

    @Override
    protected void handleMessage( RemoteMessage remoteMessage ) {
        // Custom notification implementation
        Log.d( "DEBUG", "push custom" );
    }
}
