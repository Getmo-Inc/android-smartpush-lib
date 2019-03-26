package br.com.smartpush;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.util.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class SmartpushNotification {

    private Context mContext;
    private Bundle notification;

    public enum Model {
        SIMPLE( "PUSH" ),
        BANNER( "PUSH" ),
        CAROUSEL( "CARROUSSEL" );

        private String model;

        Model(String model ) {
            this.model = model;
        }

        String getModel() {
            return model;
        }
    }

    private SmartpushNotification( Context context, Bundle data ) {
        this.mContext = context;
        notification = data;
    }

    public void createNotification() {
        new SmartpushNotificationManager( mContext ).createNotification( notification );
    }

    public static class Builder {
        private Context mContext;
        private String title, detail, banner, url, video, type = "";
        private String carousel = ", \"push.extras\": { ";
        private Boolean hasCarousel = false;

        public Builder( Context context ) {
            this.mContext = context;
        }

        public Builder title(String title){
            this.title = title;
            return this;
        }

        public Builder detail(String detail){
            this.detail = detail;
            return this;
        }

        public Builder banner(String banner){
            this.banner = banner;
            return this;
        }

        public Builder url(String url){
            this.url = url;
            return this;
        }

        public Builder video(String video){
            this.video = video;
            return this;
        }

        public Builder type( Model type ){
            this.type = type.getModel();
            return this;
        }

        public Builder carousel( ArrayList bannerUrl, ArrayList redirectUrl ){
            if( bannerUrl.size() == redirectUrl.size() ) {

                this.hasCarousel = true;

                for( int i = 0; i < bannerUrl.size(); i++ ){
                    carousel +=
                            "\"frame:"+( i + 1 ) + ":banner\":\"" + bannerUrl.get( i ) + "\"," +
                                    "\"frame:"+( i + 1 ) + ":url\":\"" + redirectUrl.get( i ) + "\",";
                }

                carousel = carousel.substring( 0, carousel.length() - 1 );
                carousel+= "}";
            }

            return this;
        }

        public SmartpushNotification build(){
            Bundle data = jsonStringToBundle( toString() );
            data.putBoolean( "notification.offline", true );
            data.putString( "provider", "smartpush" );

            return new SmartpushNotification( mContext, data );
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append( "{" );

            if ( !Strings.isEmptyOrWhitespace( title ) )
                stringBuilder.append( "\"title\":\"" + title + '\"' );

            if ( !Strings.isEmptyOrWhitespace( detail ) )
                stringBuilder.append( ", \"detail\":\"" + detail + '\"' );

            if ( !Strings.isEmptyOrWhitespace( banner ) )
                stringBuilder.append( ", \"banner\":\"" + banner + '\"' );

            if ( !Strings.isEmptyOrWhitespace( type ) )
                stringBuilder.append( ", \"type\":\"" + type + '\"' );

            if ( !Strings.isEmptyOrWhitespace( url ) )
                stringBuilder.append( ", \"url\":\"" + url + '\"' );


            if ( !Strings.isEmptyOrWhitespace( video ) )
                stringBuilder.append( ", \"video\":\"" + video + '\"' );

            if( hasCarousel ){
                stringBuilder.append( carousel );
            }

            stringBuilder.append( "}" );

            Log.d( Utils.TAG, stringBuilder.toString() );

            return stringBuilder.toString();
        }

        private static Bundle jsonStringToBundle( String jsonString ){
            try {
                return jsonToBundle( toJsonObject( jsonString ) );
            } catch ( JSONException ignored ) {
                Log.e( Utils.TAG, ignored.getMessage(), ignored );
            }
            return null;
        }

        private static JSONObject toJsonObject( String jsonString ) throws JSONException {
            return new JSONObject( jsonString );
        }

        private static Bundle jsonToBundle( JSONObject jsonObject ) throws JSONException {
            Bundle bundle = new Bundle();
            Iterator iter = jsonObject.keys();
            while( iter.hasNext() ){
                String key = ( String )iter.next();
                String value = jsonObject.getString( key );
                bundle.putString( key,value );
            }

            return bundle;
        }
    }

    public static void createSampleSimpleNotification( final Context context ){
        new Thread(new Runnable() {
            @Override
            public void run() {
                new SmartpushNotification.Builder( context )
                        .title( "Go GETMO!" )
                        .detail( "Offline Notifications!" )
                        .type( Model.SIMPLE )
                        .url( "getmo://home" )
                        .build()
                        .createNotification();
            }
        }).start();
    }

    public static void createSampleBannerNotification( final Context context ){
        new Thread(new Runnable() {
            @Override
            public void run() {
                new SmartpushNotification.Builder( context )
                        .title( "Go GETMO!" )
                        .detail( "Offline Notifications!" )
                        .type( Model.BANNER )
                        .banner( "https://pplware.sapo.pt/wp-content/uploads/2018/07/navigation-go.jpg" )
                        .url( "getmo://home" )
                        .video( "lW4pUQdRo3g" )
                        .build()
                        .createNotification();
            }
        }).start();
    }

    public static void createSampleCarouselNotification( final Context context ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                ArrayList imageList = new ArrayList<String>();
                imageList.add("https://movietvtechgeeks.com/wp-content/uploads/2017/06/xbox-one-vs-ps4-long-battle-images.jpg");
                imageList.add("https://i.pinimg.com/originals/fe/63/26/fe6326895705f9f34f250fe274ca9bf3.png");
                imageList.add("https://www.digiseller.ru/preview/115936/p1_2179893_ef9d38d0.jpg");

                ArrayList productList = new ArrayList<String>();
                productList.add("buscape://search?productId=27062&site_origem=23708552");
                productList.add("buscape://search?productId=606585&utm_source=alertadepreco&utm_medium=push&utm_campaign=606585");
                productList.add("buscape://search?productId=623321&utm_source=alertadepreco&utm_medium=push&utm_campaign=623321");

                new SmartpushNotification.Builder( context )
                        .title( "Go GETMO!" )
                        .detail( "Offline Notifications!" )
                        .type( Model.CAROUSEL )
                        .banner( "https://pplware.sapo.pt/wp-content/uploads/2018/07/navigation-go.jpg" )
                        .url( "getmo://home" )
                        .carousel( imageList, productList )
                        .build()
                        .createNotification();
            }
        }).start();
    }
}