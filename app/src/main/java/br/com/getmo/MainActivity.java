package br.com.getmo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import br.com.smartpush.Smartpush;
import br.com.smartpush.SmartpushDeviceInfo;
import br.com.smartpush.SmartpushService;
import br.com.smartpush.u.SmartpushHitUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Register at Smartpush!
        Smartpush.subscribe( this );

        // optional
        Bundle b = getIntent().getExtras();
        if ( b != null && b.containsKey( SmartpushHitUtils.Fields.PUSH_ID.getParamName() ) ) {
            Log.d( "SAMPLE", "Opened from push sent from SMARTPUSH" );

//            // Tracking
//            Smartpush.hit(
//                    MainActivity.this,
//                    SmartpushHitUtils.getValueFromPayload( SmartpushHitUtils.Fields.PUSH_ID, b ),
//                    "MAIN", null, SmartpushHitUtils.Action.CLICKED, null);
        }

//        //Tracking
//        Smartpush.hit( this, null, "MAIN", null, "OPENED", null );

//        // Nearestzone
//        Smartpush.nearestZone( this, -30.13265805301679, -51.229606855819725 );
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager
                .getInstance( this )
                .registerReceiver(mRegistrationBroadcastReceiver,
                        new IntentFilter(
                                SmartpushService.ACTION_SMARTP_REGISTRATION_RESULT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager
                .getInstance( this )
                .unregisterReceiver(mRegistrationBroadcastReceiver);
    }

//    public void onClick( View v ) {
//        // to block push
//        // Smartpush.blockPush( this, true );
//
//        // to unblock push
//        Smartpush.blockPush( this, false );
//    }

    private BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent data ) {

            if ( data.getAction().equals( SmartpushService.ACTION_SMARTP_REGISTRATION_RESULT ) ) {
                SmartpushDeviceInfo device =
                        data.getParcelableExtra(SmartpushDeviceInfo.EXTRA_DEVICE_INFO);

                TextView alias = (TextView) findViewById(R.id.alias);
                String message = (device != null) ? device.alias : "Fail :(";
                alias.setText(message);

                // set your custom TAG here!

//                // E.G:
//                // TAG type of STRING
//                Smartpush.setTag(MainActivity.this, "CARRIER", "SMARTPUSH");
//
//                // TAG type of BOOLEAN
//                Smartpush.setTag(MainActivity.this, "SHOW_ALERT_STATUS", true);
//
//                // TAG type of NUMERIC
//                Smartpush.setTag(MainActivity.this, "LAST_ORDER_VALUE", 159.88);
//
//                // TAG type of TIMESTAMP
//                Smartpush.setTag(MainActivity.this, "LAST_ORDER_DATE", new Date(0));
//
//                // TAG type of LIST
//                ArrayList<String> list = new ArrayList<>();
//                list.add("POLITICA");
//                list.add("ESPORTE");
//                list.add("ECONOMIA");
//                Smartpush.setTag(MainActivity.this, "NEWS_FEED", list);
//
//                // DELETE TAG type of STRING
//                Smartpush.delTagOrValue(MainActivity.this, "CARRIER", (String) null);
//
//                // DELETE TAG type of BOOLEAN
//                Smartpush.delTagOrValue(MainActivity.this, "SHOW_ALERT_STATUS", (Boolean) null);
//
//                // DELETE TAG type of NUMERIC
//                Smartpush.delTagOrValue(MainActivity.this, "LAST_ORDER_VALUE", (Double) null);
//
//                // DELETE TAG type of TIMESTAMP
//                Smartpush.delTagOrValue(MainActivity.this, "LAST_ORDER_DATE", (Date) null);
//
//                // DELETE TAG type of LIST
//                list.remove(0);
//                Smartpush.delTagOrValue(MainActivity.this, "NEWS_FEED", list);

            }
        }
    };
}
