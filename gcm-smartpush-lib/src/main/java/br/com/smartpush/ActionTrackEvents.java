package br.com.smartpush;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import java.util.HashMap;

class ActionTrackEvents {
    // TRACKING/ANALYTICS
    public static final String ACTION_TRACK_ACTION = "action.TRACK_ACTION";

    public static Intent startActionTrackAction( Context context, String pushId, String screenName, String category, String action, String label, boolean runAsService ) {
        if ( !( ( pushId != null ) || ( screenName != null && action != null ) ) ) {
            return null;
        }

        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_TRACK_ACTION ) ;

        if ( pushId != null )
            intent.putExtra(
                    SmartpushHitUtils.Fields.PUSH_ID.getParamName(), pushId );

        if ( screenName != null )
            intent.putExtra(
                    SmartpushHitUtils.Fields.SCREEN_NAME.getParamName(), screenName );

        if ( category != null )
            intent.putExtra(
                    SmartpushHitUtils.Fields.CATEGORY.getParamName(), category );

        if ( action != null )
            intent.putExtra(
                    SmartpushHitUtils.Fields.ACTION.getParamName(), action );

        if ( label != null )
            intent.putExtra(
                    SmartpushHitUtils.Fields.LABEL.getParamName(), label );

        if ( runAsService ) {
            context.startService(intent);
        }

        return intent;
    }

    public static void handleActionTrackAction( Context context, Intent data ) {
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
