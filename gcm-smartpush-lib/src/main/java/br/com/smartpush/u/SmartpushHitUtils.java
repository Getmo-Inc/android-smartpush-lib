package br.com.smartpush.u;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by fabio.licks on 09/02/16.
 */
public class SmartpushHitUtils {

    private Context context;

    public enum Fields {
        PUSH_ID    ( "campaignId" ),
        SCREEN_NAME( "screen_name" ),
        CATEGORY   ( "category" ),
        ACTION     ( "action" ),
        LABEL      ( "label" );

        private String paramName;

        Fields( String paramName ) {
            this.paramName = paramName;
        }

        public String getParamName() {
            return paramName;
        }
    };

    public enum Action {
        SEND, RECEIVED, CLICKED, REDIRECTED, INSTALLED, ONLINE;
    };

    public static String getValueFromPayload( Fields field, Bundle payload ) {
        return ( payload != null && payload.containsKey( field.getParamName() ) ) ? payload.getString( field.getParamName() ) : "";
    }
}
