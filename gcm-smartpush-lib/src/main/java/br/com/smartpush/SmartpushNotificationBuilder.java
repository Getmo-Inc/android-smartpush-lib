package br.com.smartpush;

import android.content.Context;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class SmartpushNotificationBuilder {

    private Context mContext;

    public enum PushModel {
        SIMPLE( "PUSH" ),
        BANNER( "PUSH" ),
        CAROUSEL( "CARROUSSEL" );

        private String model;

        private PushModel( String model ) {
            this.model = model;
        }

        public String getModel() {
            return model;
        }
    }

    public SmartpushNotificationBuilder( Context context ) {
        this.mContext = context;
    }

    private String title, detail, banner, url, video, type = "";
    private String carousel = ", \"push.extras\":{";
    private Boolean hasCarousel = false;
    private Bundle notification;

    public SmartpushNotificationBuilder title(String title){
        this.title = title;
        return this;
    }

    public SmartpushNotificationBuilder detail(String detail){
        this.detail = detail;
        return this;
    }

    public SmartpushNotificationBuilder banner(String banner){
        this.banner = banner;
        return this;
    }

    public SmartpushNotificationBuilder url(String url){
        this.url = url;
        return this;
    }

    public SmartpushNotificationBuilder video(String video){
        this.video = video;
        return this;
    }

    public SmartpushNotificationBuilder type( PushModel type ){
        this.type = type.getModel();
        return this;
    }

    public SmartpushNotificationBuilder carousel(ArrayList bannerUrl, ArrayList redirectUrl){
        if(bannerUrl.size() == redirectUrl.size()){

            this.hasCarousel = true;

            for(int i = 0; i < bannerUrl.size(); i++){

                carousel += "\"frame:"+(i+1)+":banner\":\""+bannerUrl.get(i)+"\"," +
                        "\"frame:"+(i+1)+":url\":\""+redirectUrl.get(i)+"\",";
            }

            carousel = carousel.substring(0,carousel.length()-1);
            carousel+= "}";

        }

        return this;
    }

    public SmartpushNotificationBuilder build(){
        this.notification = jsonStringToBundle( selfToString() );
        this.notification.putBoolean( "extra.hit", false );
        return this;
    }

    public void createNotification(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                new SmartpushNotificationManager(mContext).createNotification(notification);
            }
        }).start();

    }

    String selfToString() {

        String retorno = "{" +
                "title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                ", banner='" + banner + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", video='" + video + '\'' +
                '}';

        if(hasCarousel){
            retorno = retorno.substring(0,retorno.length()-1);
            retorno += carousel + "}";
        }

        return retorno;
    }

    private static Bundle jsonStringToBundle(String jsonString){
        try {
            JSONObject jsonObject = toJsonObject(jsonString);
            return jsonToBundle(jsonObject);
        } catch (JSONException ignored) {

        }
        return null;
    }

    private static JSONObject toJsonObject(String jsonString) throws JSONException {
        return new JSONObject(jsonString);
    }

    private static Bundle jsonToBundle(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator iter = jsonObject.keys();
        while(iter.hasNext()){
            String key = (String)iter.next();
            String value = jsonObject.getString(key);
            bundle.putString(key,value);
        }
        return bundle;
    }

    /* SAMPLE */
    // Simple
    public void notificationSample(){
//        final String notification =
//                "{\n" +
//                "        \"type\": \"PUSH\",\n" +
//                "        \"provider\": \"smartpush\",\n" +
//                "        \"title\": \"Go Getmo\",\n" +
//                "        \"detail\": \"Notificações que engajam!\",\n" +
//                "        \"url\": \"getmo://home\",\n" +
//                "        \"video\": \"lW4pUQdRo3g\"\n" +
//                "}";
        new Thread(new Runnable() {
            @Override
            public void run() {
//                new SmartpushNotificationManager( mContext )
//                        .createNotification( jsonStringToBundle( notification ) );

                new SmartpushNotificationBuilder( mContext )
                        .title( "Go GETMO!" )
                        .detail( "Offline Notifications!" )
                        .type( PushModel.SIMPLE )
                        .url( "getmo://home" )
                        .build()
                        .createNotification();
            }
        }).start();
    }

    // Banner
    public void bannerNotificataionSample(){
//        final String banner =
//                "{\n" +
//                "        \"type\": \"PUSH\",\n" +
//                "        \"provider\": \"smartpush\",\n" +
//                "        \"title\": \"Go Getmo\",\n" +
//                "        \"detail\": \"Notificações que engajam!\",\n" +
//                "        \"banner\": \"https://pplware.sapo.pt/wp-content/uploads/2018/07/navigation-go.jpg\",\n" +
//                "        \"url\": \"getmo://home\",\n" +
//                "        \"video\": \"lW4pUQdRo3g\"\n" +
//                "}";

        new Thread(new Runnable() {
            @Override
            public void run() {
//                new SmartpushNotificationManager( mContext )
//                        .createNotification( jsonStringToBundle( banner ) );

                new SmartpushNotificationBuilder( mContext )
                        .title( "Go GETMO!" )
                        .detail( "Offline Notifications!" )
                        .type( PushModel.BANNER )
                        .banner( "https://pplware.sapo.pt/wp-content/uploads/2018/07/navigation-go.jpg" )
                        .url( "getmo://home" )
                        .build()
                        .createNotification();

            }
        }).start();
    }

    // Carousel
    public void carouselNotificationSample() {
//        final String carousel =
//                "{\n" +
//                    "\"banner\":\"https://movietvtechgeeks.com/wp-content/uploads/2017/06/xbox-one-vs-ps4-long-battle-images.jpg\",\n" +
//                    "\"detail\":\"Escolhemos ofertas especiais para você!\",\n" +
//                    "\"provider\":\"smartpush\",\n" +
//                    "\"push.extras\":{\n" +
//                        "\"frame:1:banner\":\"https:\\/\\/movietvtechgeeks.com\\/wp-content\\/uploads\\/2017\\/06\\/xbox-one-vs-ps4-long-battle-images.jpg\",\n" +
//                        "\"frame:1:url\":\"buscape:\\/\\/search?productId=27062&site_origem=23708552\",\n" +
//                        "\"frame:2:banner\":\"https:\\/\\/i.pinimg.com\\/originals\\/fe\\/63\\/26\\/fe6326895705f9f34f250fe274ca9bf3.png\",\n" +
//                        "\"frame:2:url\":\"buscape:\\/\\/search?productId=606585&utm_source=alertadepreco&utm_medium=push&utm_campaign=606585\",\n" +
//                        "\"frame:3:banner\":\"https:\\/\\/www.digiseller.ru\\/preview\\/115936\\/p1_2179893_ef9d38d0.jpg\",\n" +
//                        "\"frame:3:url\":\"buscape:\\/\\/search?productId=623321&utm_source=alertadepreco&utm_medium=push&utm_campaign=623321\"\n" +
//                    "},\n" +
//                    "\"url\":\"buscape://search?productId=27062&site_origem=23708552\", \n" +
//                    "\"type\":\"CARROUSSEL\", \n" +
//                    "\"title\":\"Buscape\" \n" +
//                "}";

        new Thread( new Runnable() {
            @Override
            public void run() {
//                new SmartpushNotificationManager( mContext )
//                        .createNotification( jsonStringToBundle( carousel ) );

                ArrayList imageList = new ArrayList<String>();
                imageList.add("https://movietvtechgeeks.com/wp-content/uploads/2017/06/xbox-one-vs-ps4-long-battle-images.jpg");
                imageList.add("https://i.pinimg.com/originals/fe/63/26/fe6326895705f9f34f250fe274ca9bf3.png");
                imageList.add("https://www.digiseller.ru/preview/115936/p1_2179893_ef9d38d0.jpg");

                ArrayList productList = new ArrayList<String>();
                productList.add("buscape://search?productId=27062&site_origem=23708552");
                productList.add("buscape://search?productId=606585&utm_source=alertadepreco&utm_medium=push&utm_campaign=606585");
                productList.add("buscape://search?productId=623321&utm_source=alertadepreco&utm_medium=push&utm_campaign=623321");

                new SmartpushNotificationBuilder( mContext )
                        .title( "Go GETMO!" )
                        .detail( "Offline Notifications!" )
                        .type( PushModel.CAROUSEL )
                        .banner( "https://pplware.sapo.pt/wp-content/uploads/2018/07/navigation-go.jpg" )
                        .url( "getmo://home" )
                        .carousel( imageList, productList )
                        .build()
                        .createNotification();
            }
        }).start();
    }
}