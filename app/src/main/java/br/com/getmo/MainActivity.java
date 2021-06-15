package br.com.getmo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.content.LocalBroadcastManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.util.Strings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import br.com.smartpush.Smartpush;
import br.com.smartpush.SmartpushDeviceInfo;
import br.com.smartpush.SmartpushNotification;
import br.com.smartpush.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private final static int PUSH_BANNER = 0;
    private final static int PUSH_CAROUSEL = 1;

    private final static String TAG = "LOG";
    private TextView log;
    private Spinner spinner;

    private int spinnerOption = 0;

    private boolean pushEnabled;

    private String pushid = "";


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        log = findViewById( R.id.log );

        initSpinner();

        Smartpush.subscribe( this );

        // optional - Tracking :: Call this method always! if app was opened by push one event will
        // be saved, if no nothing will happen.
//        Smartpush.hitClick( this, getIntent().getExtras() );

//        //Tracking
//        Smartpush.hit( this, null, "MAIN", null, "OPENED", null );

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
        arrayAdapter.add("OFFLINE SIMPLE NOTIFICATION");
        arrayAdapter.add("OFFLINE BANNER NOTIFICATION");
        arrayAdapter.add("OFFLINE CAROUSEL NOTIFICATION");
        arrayAdapter.add("GET GEOZONES");
        arrayAdapter.add("MARK ALL MESSAGES AS READ");
        arrayAdapter.add("GET LAST UNREAD MESSAGES");
        arrayAdapter.add("MARK MESSAGE AS READ");
        arrayAdapter.add("GET LAST MESSAGES");
        arrayAdapter.add("PUSH");
        arrayAdapter.add("PUSH CAROUSEL");

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

        LocalBroadcastManager
                .getInstance( this )
                .registerReceiver( receiverMarkAllRead,
                        new IntentFilter( Smartpush.ACTION_MARK_ALL_NOTIF_AS_READ ) );

        LocalBroadcastManager
                .getInstance( this )
                .registerReceiver( receiverLast10Unread,
                        new IntentFilter( Smartpush.ACTION_LAST_10_UNREAD_NOTIF ) );

        LocalBroadcastManager
                .getInstance( this )
                .registerReceiver( receiverMarkRead,
                        new IntentFilter( Smartpush.ACTION_MARK_NOTIF_AS_READ ) );

        LocalBroadcastManager
                .getInstance( this )
                .registerReceiver( receiverLast10,
                        new IntentFilter( Smartpush.ACTION_LAST_10_NOTIF ) );
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager
                .getInstance( this )
                .unregisterReceiver( mRegistrationBroadcastReceiver );

        LocalBroadcastManager
                .getInstance( this )
                .unregisterReceiver( mGetTagValuesBroadcastReceiver );

        LocalBroadcastManager
                .getInstance( this )
                .unregisterReceiver( mGetDeviceInfoBroadcastReceiver );

        LocalBroadcastManager
                .getInstance( this )
                .unregisterReceiver( receiverMarkAllRead );

        LocalBroadcastManager
                .getInstance( this )
                .unregisterReceiver( receiverLast10Unread );

        LocalBroadcastManager
                .getInstance( this )
                .unregisterReceiver( receiverMarkRead );

        LocalBroadcastManager
                .getInstance( this )
                .unregisterReceiver( receiverLast10 );
    }

    public void push( int push ){
        String json = getPushConfig( push );

        Call<ResponseBody> call =
                ApiClient
                        .getClient()
                        .create(ApiInterface.class)
                        .sendPushNotification( json );

        call.enqueue( new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d( TAG, "RESPONSE SUCCESS" );
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d( TAG, "RESPONSE FAIL");
            }
        });
    }

    public void onClick( View v ) {
        if (v.getId() == R.id.btn_execute_spinner_action) {

            switch (spinnerOption){
                case 0:
                    Smartpush.getUserInfo(this);
                    break;

                case 1:
                    boolean status = Smartpush.areNotificationsEnabled(this);
                    Log.d( TAG, "Notification State: " + status );
                    log.setText( "NOTIFICATION STATUS: " + ( status ? "ENABLE" : "DISABLE" ) );
                    break;

                case 2:
                    Smartpush.blockPush(this, pushEnabled);
                    pushEnabled = !pushEnabled;
                    String state = ( pushEnabled ) ? "ENABLED" : "DISABLED";
                    Log.d( TAG, "PUSH Status: " + state );
                    log.setText( "PUSH Status: " + state );
                    break;

                case 3:
                    setTags();
                    break;

                case 4:
                    delTags();
                    break;

                case 5:
                    Smartpush.getTagValues(this, "NEWS_FEED");
                    break;

                case 6:
                    SmartpushNotification.createSampleSimpleNotification( this );
                    break;

                case 7:
                    SmartpushNotification.createSampleBannerNotification( this );
                    break;

                case 8:
                    SmartpushNotification.createSampleCarouselNotification( this );
                    break;

                case 9:
                    String geo = Smartpush.getGeozones(this);
                    Log.d(TAG, "GEOZONES: "+ geo);
                    log.setText("GEOZONES: \n"+geo);
                    break;

                case 10:
                    Smartpush.markAllMessagesAsRead(this);
                    break;

                case 11:
                    Smartpush.getLastUnreadMessages(this, null);
                    break;

                case 12:
                    Smartpush.markMessageAsRead(this, pushid);
                    break;

                case 13:
                    Smartpush.getLastMessages(this, null);
                    break;

                case 14:
                    push( PUSH_BANNER );
                    break;

                case 15:
                    push( PUSH_CAROUSEL );
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
                Log.d(TAG, "DEVICE_INFO.DATA: " + (deviceInfo != null ? deviceInfo.toString() : "FAIL"));
                log.setText("GET DEVICE_INFO:");
                log.append("\nDEVICE_INFO.DATA: " + (deviceInfo != null ? deviceInfo.toString() : "FAIL"));
                pushEnabled = (deviceInfo != null && "0".equals(deviceInfo.optout)) ? true : false;
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
                    Smartpush.getUserInfo( MainActivity.this );
                    Smartpush.setTag(MainActivity.this, "SMARTPUSH_ID", device.alias );

                    Smartpush.nearestZone( MainActivity.this, -30.0586387,-51.175569 );
                }
            }
        }
    };

    private BroadcastReceiver receiverLast10 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data ) {
            if ( data.getAction().equals( Smartpush.ACTION_LAST_10_NOTIF ) ) {
                processInbox( data, "GET LAST 10 MESSAGES:" );
            }
        }
    };

    private BroadcastReceiver receiverMarkAllRead = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data) {
            if ( data.getAction().equals( Smartpush.ACTION_MARK_ALL_NOTIF_AS_READ ) ) {
                processInbox( data, "MARK ALL NOTIF AS READ:" );
            }
        }
    };

    private BroadcastReceiver receiverLast10Unread = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data) {
            if ( data.getAction().equals( Smartpush.ACTION_LAST_10_UNREAD_NOTIF ) ) {
                processInbox( data, "GET LAST UNREAD MESSAGES:" );
            }
        }
    };

    private BroadcastReceiver receiverMarkRead = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data ) {
            if ( data.getAction().equals( Smartpush.ACTION_MARK_NOTIF_AS_READ) ) {
                processInbox( data, "MARK NOTIF AS READ:" );
            }
        }
    };

    private String getPushConfig( int push ) {
        try {
            String jsonString =
                    FileManager.getStringFromInputStream( getAssets().open( "push_config.json" ) );

            JSONObject json =
                    new JSONArray( jsonString ).getJSONObject( push );

            json.getJSONObject( "payload" )
                    .getJSONObject( "filter" )
                    .put( "alias",
                            Utils.PreferenceUtils.readFromPreferences(this, Utils.Constants.SMARTP_ALIAS ) );

            Log.d( "LOG", json.toString(  ) );

            return json.getJSONObject( "payload" ).toString();
        } catch ( IOException | JSONException e ) {
            Log.e( "LOG", e.getMessage(), e );
        }

        return null;
    }

    private void processInbox( Intent data, String messagePrefix ) {
        log.setText( messagePrefix );
        log.append( "\n" );

        try {
            JSONArray array = new JSONArray( data.getStringExtra( Smartpush.EXTRA_VALUE ) );
            if ( array.length() > 0 ) {
                JSONObject obj = array.getJSONObject(0 );
                if ( obj.has( "pushid" ) ) {
                    pushid = obj.getString("pushid");
                }
                log.append( array.toString( 4 ) );
                return;
            }
        } catch ( JSONException e ) {
            Log.e( TAG, e.getMessage(), e );
        }

        log.append( "Não há mensagens." );
    }
}