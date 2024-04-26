package br.com.smartpush;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.util.Strings;

import java.io.StringBufferInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

class ActionTrackEvents {
    // TRACKING/ANALYTICS
    public static final String ACTION_TRACK_ACTION = "action.TRACK_ACTION";

    public static Intent startActionTrackAction( Context context, String alias, String pushId, String screenName, String category, String action, String label, boolean runAsService ) {
        if ( !( ( pushId != null ) || ( screenName != null && action != null ) ) ) {
            return null;
        }

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_TRACK_ACTION ) ;

        if ( !Strings.isEmptyOrWhitespace( alias ) ) {
            intent.putExtra("alias", alias);
        } else {
            intent.putExtra("alias",
                    new SimpleDateFormat( "yyyyMMdd" )
                            .format( Calendar.getInstance().getTime() ));
        }

        if ( !Strings.isEmptyOrWhitespace( pushId ) )
            intent.putExtra(
                    SmartpushHitUtils.Fields.PUSH_ID.getParamName(), pushId );

        if ( !Strings.isEmptyOrWhitespace( screenName ) )
            intent.putExtra(
                    SmartpushHitUtils.Fields.SCREEN_NAME.getParamName(), screenName );

        if ( !Strings.isEmptyOrWhitespace( category ) )
            intent.putExtra(
                    SmartpushHitUtils.Fields.CATEGORY.getParamName(), category );

        if ( !Strings.isEmptyOrWhitespace( action ) )
            intent.putExtra(
                    SmartpushHitUtils.Fields.ACTION.getParamName(), action );

        if ( !Strings.isEmptyOrWhitespace( label ) )
            intent.putExtra(
                    SmartpushHitUtils.Fields.LABEL.getParamName(), label );

        if ( runAsService ) {
            SmartpushService.start(intent, context);
        }

        return intent;
    }

    public static void handleActionTrackAction( Context context, Intent data ) {
        SmartpushLog.d( Utils.TAG, "----------> " + Utils.ArrayUtils.bundle2string( data.getExtras() ) );

        HashMap<String,String> fields = new HashMap<>();
        fields.put( "uuid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_HWID ) );

        fields.put( "appid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_APP_ID) );

        fields.put( "devid",
                Utils.Smartpush.getMetadata(
                        context, Utils.Constants.SMARTP_API_KEY ) );

        fields.put( "regid",
                Utils.PreferenceUtils.readFromPreferences(
                        context, Utils.Constants.SMARTP_REGID ) );

        fields.put( "framework",
                Build.VERSION.RELEASE );

        fields.put( "sdk_v",
                context.getString( R.string.smartp_version ) );

        fields.put( "plataformId", "ANDROID" );

        Bundle bundle = data.getExtras();

        String pushId = "";
        String alias  = "";

        if ( bundle != null
                && bundle.containsKey( SmartpushHitUtils.Fields.PUSH_ID.getParamName() ) ) {
            pushId = bundle.getString( SmartpushHitUtils.Fields.PUSH_ID.getParamName() );
            fields.put( SmartpushHitUtils.Fields.PUSH_ID.getParamName(), pushId );
        }

        if ( bundle != null && bundle.containsKey( "alias" ) ) {
            alias = bundle.getString( "alias" );
        } else {
            alias = new SimpleDateFormat( "yyyyMMdd" ).format( Calendar.getInstance().getTime() );
        }

        if ( bundle != null
                && bundle.containsKey( SmartpushHitUtils.Fields.SCREEN_NAME.getParamName() ) )
            fields.put( SmartpushHitUtils.Fields.SCREEN_NAME.getParamName(),
                    bundle.getString( SmartpushHitUtils.Fields.SCREEN_NAME.getParamName() ) );

        if ( bundle != null
                && bundle.containsKey( SmartpushHitUtils.Fields.CATEGORY.getParamName() ) )
            fields.put( SmartpushHitUtils.Fields.CATEGORY.getParamName(),
                    bundle.getString( SmartpushHitUtils.Fields.CATEGORY.getParamName() ) );

        String action = "";
        if ( bundle != null
                && bundle.containsKey( SmartpushHitUtils.Fields.ACTION.getParamName() ) ) {
            action = bundle.getString( SmartpushHitUtils.Fields.ACTION.getParamName() );
            fields.put( SmartpushHitUtils.Fields.ACTION.getParamName(), action );
        }

        if ( bundle != null
                && bundle.containsKey( SmartpushHitUtils.Fields.LABEL.getParamName() ) ) {
            fields.put( SmartpushHitUtils.Fields.LABEL.getParamName(),
                    bundle.getString( SmartpushHitUtils.Fields.LABEL.getParamName() ) );
        }

        if ( !SmartpushHitUtils.Action.RECEIVED.name().equals( action ) &&
                !SmartpushHitUtils.Action.REJECTED.name().equals( action ) ) {
            SmartpushHttpClient.post( "hit", fields, context, false );
        }

        SmartpushHttpClient.sendToAnalytics( context, alias, pushId, action );
    }
}
