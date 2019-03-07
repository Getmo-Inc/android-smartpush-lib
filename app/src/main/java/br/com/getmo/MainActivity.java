package br.com.getmo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

import java.util.ArrayList;
import java.util.Date;

import br.com.smartpush.Smartpush;
import br.com.smartpush.SmartpushDeviceInfo;
import br.com.smartpush.SmartpushNotificationBuilder;
import br.com.smartpush.SmartpushService;
import br.com.smartpush.Utils;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "LOG";
    private TextView log;
    private Spinner spinner;

    private int spinnerOption = 0;

    // TODO ajustar a inicializacao desta variavel...
    private boolean pushStatus;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        log = findViewById( R.id.log );

        initSpinner();

        Smartpush.subscribe( this );

        /*ArrayList imageList = new ArrayList<String>();
        imageList.add("https://movietvtechgeeks.com/wp-content/uploads/2017/06/xbox-one-vs-ps4-long-battle-images.jpg");
        imageList.add("https://i.pinimg.com/originals/fe/63/26/fe6326895705f9f34f250fe274ca9bf3.png");

        ArrayList productList = new ArrayList<String>();
        productList.add("buscape://search?productId=27062&site_origem=23708552");
        productList.add("buscape://search?productId=606585&utm_source=alertadepreco&utm_medium=push&utm_campaign=606585");

        new SmartpushNotificationBuilder(this)
                .title("GO GETMO !")
                .detail("Getmo Offline Notifications!")
                .type("CARROUSSEL")
                .banner("https://pplware.sapo.pt/wp-content/uploads/2018/07/navigation-go.jpg")
                .url("getmo://home")
                .video("lW4pUQdRo3g")
                .carousel(imageList, productList)
                .build()
                .createNotification();*/

        /* Offline Notification Sample */

        // Notification
//        new SmartpushNotificationBuilder(this).notificationSample();

        // Banner Notification
//        new SmartpushNotificationBuilder(this).bannerNotificataionSample();

        // Carousel Notification
//        new SmartpushNotificationBuilder(this).carouselNotificationSample();

        /* End Offline Sample */

        // optional - Tracking :: Call this method always! if app was opened by push one event will
        // be saved, if no nothing will happen.
//        Smartpush.hitClick( this, getIntent().getExtras() );

//        //Tracking
//        Smartpush.hit( this, null, "MAIN", null, "OPENED", null );

//        // Nearestzone
//        Smartpush.nearestZone( this, -30.13265805301679, -51.229606855819725 );
//        Smartpush.nearestZone( this, -30.132658053017, -51.22960685582 );

//        new SmartpushNotificationManager( this ).scheduleNotificationRefreshTime();

//        SmartpushService.getAppList( this );

        /*Smartpush.getLastMessages(this, new Date(0));
        Smartpush.getLastUnreadMessages(this, new Date(0));
        Smartpush.getGeozones(this);*/

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data      = intent.getData();

        Log.d( TAG, "[" + action + "] : " + ( data != null ? data.toString() : "NO DATA! " ) );
    }

    private void initSpinner() {
        spinner = findViewById(R.id.spinner_lib_functions);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        arrayAdapter.add("GET DEVICE INFO");
        arrayAdapter.add("STATUS NOTIFICATION");
        arrayAdapter.add("(UN)BLOCK PUSH");
        arrayAdapter.add("SET TAG");
        arrayAdapter.add("DEL TAG");
        arrayAdapter.add("GET TAG");
        arrayAdapter.add("SIMPLE OFFLINE NOTIFICATION");
        arrayAdapter.add("BANNER OFFLINE NOTIFICATION");
        arrayAdapter.add("CAROUSEL OFFLINE NOTIFICATION");

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { spinnerOption = position; }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { spinnerOption = 0; }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager
                .getInstance( this )
                .registerReceiver( mRegistrationBroadcastReceiver,
                        new IntentFilter(
                                Smartpush.ACTION_REGISTRATION_RESULT ) );

        LocalBroadcastManager
                .getInstance( this )
                .registerReceiver( mGetTagValuesBroadcastReceiver,
                        new IntentFilter(
                                Smartpush.ACTION_GET_TAG_VALUES ) );

        LocalBroadcastManager
                .getInstance( this )
                .registerReceiver( mGetDeviceInfoBroadcastReceiver,
                        new IntentFilter(
                                Smartpush.ACTION_GET_DEVICE_USER_INFO ) );
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager
                .getInstance( this )
                .unregisterReceiver(mRegistrationBroadcastReceiver);

        LocalBroadcastManager
                .getInstance( this )
                .unregisterReceiver(mGetTagValuesBroadcastReceiver);

        LocalBroadcastManager
                .getInstance( this )
                .unregisterReceiver(mGetDeviceInfoBroadcastReceiver);
    }

    public void onClick( View v ) {
        if (v.getId() == R.id.btn_execute_spinner_action) {
            switch (spinnerOption){
                case 0: Smartpush.getUserInfo(this);
                    break;

                case 1: boolean status = Smartpush.areNotificationsEnabled(this);
                    Log.d( TAG, "Notification State: " + status );
                    log.setText( "NOTIFICATION STATUS: " + ( status ? "ENABLE" : "DISABLE" ) );
                    break;

                case 2: pushStatus = !pushStatus;
                    Smartpush.blockPush(this, pushStatus );
                    Log.d( TAG, "PUSH Status: " + ( !pushStatus ? "ENABLE" : "DISABLE" ) );
                    log.setText( "PUSH Status: " + ( !pushStatus ? "ENABLE" : "DISABLE" ) );
                    break;

                case 3: setTags();
                    break;

                case 4: delTags();
                    break;

                case 5: Smartpush.getTagValues(this, "NEWS_FEED");
                    break;

                case 6: new SmartpushNotificationBuilder(this).notificationSample();
                    break;

                case 7: new SmartpushNotificationBuilder(this).bannerNotificataionSample();
                    break;

                case 8: new SmartpushNotificationBuilder(this).carouselNotificationSample();
                    break;
            }
        }
    }

    private void setTags() {
        log.setText( "SET TAGS:" );

        // TAG type of LIST
        ArrayList<String> list = new ArrayList<>();
        list.add("POLITICA");
        list.add("ESPORTE");
        list.add("ECONOMIA");
        list.add("MODA");

        // Samples:
        // TAG type of STRING
        Smartpush.setTag(this, "CARRIER", "UNDEFINED");
        log.append( "\ntag.CARRIER = UNDEFINED" );

        // TAG type of BOOLEAN
        Smartpush.setTag(this, "SHOW_ALERT_STATUS", true );
        log.append( "\ntag.SHOW_ALERT_STATUS = true" );

        // TAG type of NUMERIC
        Smartpush.setTag(this, "LAST_ORDER_VALUE", 159.88 );
        log.append( "\ntag.LAST_ORDER_VALUE = 159.88" );

        // TAG type of TIMESTAMP
        Smartpush.setTag(this, "LAST_ORDER_DATE", new Date( 0 ) );
        log.append( "\ntag.LAST_ORDER_DATE = " + String.valueOf( ( new Date( 0 ) ).getTime() / 1000 )  );

        Smartpush.setTag( this, "NEWS_FEED", list );
        log.append( "\ntag.NEWS_FEED = " + TextUtils.join(", ", list ) );

        // GET TAG VALUE
        // Testing insertion of a empty list
        Smartpush.setTag( this, "APPS_LIST", new ArrayList<String>() );

        // Testing insertion of a null list
        Smartpush.setTag( this, "tagList", (ArrayList<String>) null );
    }

    private void delTags() {
        log.setText( "DEL TAGS:" );
        // TAG type of LIST
        ArrayList<String> list = new ArrayList<>();
        list.add("POLITICA");
        list.add("ESPORTE");
        list.add("ECONOMIA");
        list.add("MODA");

        // DELETE TAG type of STRING
        Smartpush.delTagOrValue(this, "CARRIER", (String) null);
        log.append( "\ntag.CARRIER" );

        // DELETE TAG type of BOOLEAN
        Smartpush.delTagOrValue(this, "SHOW_ALERT_STATUS", (Boolean) null);
        log.append( "\ntag.SHOW_ALERT_STATUS" );

        // DELETE TAG type of NUMERIC
        Smartpush.delTagOrValue(this, "LAST_ORDER_VALUE", (Double) null);
        log.append( "\ntag.LAST_ORDER_VALUE" );

        // DELETE TAG type of TIMESTAMP
        Smartpush.delTagOrValue(this, "LAST_ORDER_DATE", (Date) null);
        log.append( "\ntag.LAST_ORDER_DATE" );

        // DELETE TAG type of LIST
        list.remove(0);
        Smartpush.delTagOrValue(this, "NEWS_FEED", list);
        log.append( "\ntag.NEWS_FEED = " + TextUtils.join(", ", list ) );
    }

    private BroadcastReceiver mGetTagValuesBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data) {
            String dados = data.getStringExtra( Smartpush.EXTRA_VALUE );
            Log.d( TAG,"TAG.DATA: " + dados );
            log.setText( "GET TAG NEWS_FEED:" );
            log.append( "\nTAG.DATA: " + dados );
        }
    };

    private BroadcastReceiver mGetDeviceInfoBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data) {
            SmartpushDeviceInfo deviceInfo =
                    data.getParcelableExtra( Smartpush.EXTRA_DEVICE_INFO );
            Log.d( TAG,"DEVICE_INFO.DATA: " + ( deviceInfo != null ? deviceInfo.toString() : "FAIL" ) );
            log.setText( "GET DEVICE_INFO:" );
            log.append( "\nDEVICE_INFO.DATA: " + ( deviceInfo != null ? deviceInfo.toString() : "FAIL" ) );
        }
    };

    private BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent data ) {
            if ( data.getAction().equals( Smartpush.ACTION_REGISTRATION_RESULT) ) {
                SmartpushDeviceInfo device =
                        data.getParcelableExtra( Smartpush.EXTRA_DEVICE_INFO );

                boolean registered = ( device != null && !Strings.isEmptyOrWhitespace( device.alias ) );

                TextView alias = findViewById( R.id.alias );
                String message = ( registered ) ? device.alias : "Fail :(";
                alias.setText( message );

                // set your user id TAG here!
                if ( registered ) {
                    Smartpush.setTag(MainActivity.this, "SMARTPUSH_ID", device.alias );
                }
            }
        }
    };
}