package br.com.smartpush;

//import com.google.android.gms.iid.InstanceIDListenerService;
import com.google.firebase.messaging.FirebaseMessagingService;

/**
 * Created by fabio.licks on 09/02/16.
 */
public final class SmartpushIDListenerService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        SmartpushService.subscrive(this, s);
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]

    /*@Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        SmartpushService.subscrive( this );
    }*/
    // [END refresh_token]
}
