package br.com.smartpush;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

class ActionPushInbox {
    public  static final String ACTION_HIDE_NOTIF = "action.HIDE_NOTIF";

    public static void startActionLastMessages( Context context, Date startingDate ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( Smartpush.ACTION_LAST_10_NOTIF ) ;

        if ( startingDate != null ) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // This line converts the given date into UTC time zone
            formatter.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
            intent.putExtra( Smartpush.EXTRA_VALUE, formatter.format( startingDate ) );
        }

        context.startService( intent );
    }

    public static void startActionLastUnreadMessage( Context context, Date startingDate ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( Smartpush.ACTION_LAST_10_UNREAD_NOTIF ) ;

        if ( startingDate != null ) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // This line converts the given date into UTC time zone
            formatter.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
            intent.putExtra( Smartpush.EXTRA_VALUE, formatter.format( startingDate ) );
        }

        context.startService( intent );
    }

    public static void startActionMarkMessageAsRead( Context context, String pushId ) {
        if ( pushId == null ) return;

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( Smartpush.ACTION_MARK_NOTIF_AS_READ ) ;
        intent.putExtra( Smartpush.EXTRA_VALUE, pushId );
        context.startService( intent );
    }

    public static void startActionHideMessage( Context context, String pushId ) {
        if ( pushId == null ) return;

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_HIDE_NOTIF ) ;
        intent.putExtra( Smartpush.EXTRA_VALUE, pushId );
        context.startService( intent );
    }

    public static void startActionMarkAllMessagesAsRead( Context context ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( Smartpush.ACTION_MARK_ALL_NOTIF_AS_READ ) ;
        context.startService( intent );
    }

    public static void startActionGetMessageExtraPayload( Context context, String pushId ) {
        if ( pushId == null ) return;

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( Smartpush.ACTION_GET_NOTIF_EXTRA_PAYLOAD ) ;
        intent.putExtra( Smartpush.EXTRA_VALUE, pushId );
        context.startService( intent );
    }


    public static void handleActionLastMessages( Context context, Intent data ) {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put( "devid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_API_KEY ) );

        params.put( "appid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_APP_ID ) );

        params.put( "hwid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_HWID ) );

        params.put( "regid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_REGID ) );

        params.put( "platform", "ANDROID" );

        if ( data.hasExtra( Smartpush.EXTRA_VALUE ) ) {
            params.put( "startingDate", data.getStringExtra( Smartpush.EXTRA_VALUE ) );
        }

        params.put( "dateFormat", "d/m/Y H:i:s" );

        LocalBroadcastManager
                .getInstance( context )
                .sendBroadcast(
                        new Intent( Smartpush.ACTION_LAST_10_NOTIF )
                                .putExtra( Smartpush.EXTRA_VALUE, SmartpushHttpClient
                                        .post( "notifications/last", params, context, false ) ) );
    }

    public static void handleActionLastUnreadMessages( Context context, Intent data ) {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put( "devid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_API_KEY ) );

        params.put( "appid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_APP_ID ) );

        params.put( "hwid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_HWID ) );

        params.put( "regid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_REGID ) );

        params.put( "platform", "ANDROID" );

        if ( data.hasExtra( Smartpush.EXTRA_VALUE ) ) {
            params.put( "startingDate", data.getStringExtra( Smartpush.EXTRA_VALUE ) );
        }

        params.put( "dateFormat", "d/m/Y H:i:s" );

        LocalBroadcastManager
                .getInstance( context )
                .sendBroadcast(
                        new Intent( Smartpush.ACTION_LAST_10_UNREAD_NOTIF )
                                .putExtra( Smartpush.EXTRA_VALUE, SmartpushHttpClient
                                        .post( "notifications/unread", params, context, false ) ) );
    }

    public static void handleActionGetMessageExtraPayload( Context context, Intent data ) {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put( "devid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_API_KEY ) );

        params.put( "appid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_APP_ID ) );

        params.put( "pushid",
                data.getStringExtra( Smartpush.EXTRA_VALUE ) );

        params.put( "regid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_REGID ) );

        LocalBroadcastManager
                .getInstance( context )
                .sendBroadcast(
                        new Intent( Smartpush.ACTION_GET_NOTIF_EXTRA_PAYLOAD )
                                .putExtra( Smartpush.EXTRA_VALUE, SmartpushHttpClient
                                        .post( "notifications/extra", params, context, false ) ) );
    }

    public static void handleActionMarkMessageAsRead( Context context, Intent data ) {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put( "devid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_API_KEY ) );

        params.put( "appid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_APP_ID ) );

        params.put( "hwid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_HWID ) );

        params.put( "pushid",
                data.getStringExtra( Smartpush.EXTRA_VALUE ) );

        params.put( "regid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_REGID ) );

        params.put( "_method", "DELETE");

        LocalBroadcastManager
                .getInstance( context )
                .sendBroadcast(
                        new Intent( Smartpush.ACTION_MARK_NOTIF_AS_READ )
                                .putExtra( Smartpush.EXTRA_VALUE, SmartpushHttpClient
                                        .post( "notifications/read-one", params, context, false ) ) );
    }

    public static void handleActionMarkAllMessagesAsRead( Context context, Intent data ) {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put( "devid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_API_KEY ) );

        params.put( "appid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_APP_ID ) );

        params.put( "hwid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_HWID ) );

        params.put( "regid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_REGID ) );

        params.put( "_method", "DELETE");

        LocalBroadcastManager
                .getInstance( context )
                .sendBroadcast(
                        new Intent( Smartpush.ACTION_MARK_ALL_NOTIF_AS_READ )
                                .putExtra( Smartpush.EXTRA_VALUE, SmartpushHttpClient
                                        .post( "notifications/read-all", params, context, false ) ) );
    }

    public static void handleActionHideMessage( Context context, Intent data ) {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put( "devid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_API_KEY ) );

        params.put( "appid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_APP_ID ) );

        params.put( "pushid",
                data.getStringExtra( Smartpush.EXTRA_VALUE ) );

        params.put( "hwid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_HWID ) );

        params.put( "regid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_REGID ) );

        params.put( "_method", "PUT");

        SmartpushHttpClient
                .post( "notifications/hide", params, context, false );
    }

}
