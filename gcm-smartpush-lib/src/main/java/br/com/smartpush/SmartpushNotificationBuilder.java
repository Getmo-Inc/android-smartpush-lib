package br.com.smartpush;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class SmartpushNotificationBuilder {

    private Context mContext;

    public SmartpushNotificationBuilder(Context context) {
        this.mContext = context;
    }

    private String title, detail, banner, url, video, type = "";
    private String carousel = "";
    private ArrayList carouselList = new ArrayList<String>();

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

    public SmartpushNotificationBuilder type(String type){
        this.type = type;
        return this;
    }

//    public SmartpushNotificationBuilder carousel(ArrayList bannerUrl, ArrayList redirectUrl){
//        this.carousel = new Carousel(bannerUrl, redirectUrl).processCarousel();
//        return this;
//    }

    public SmartpushNotificationBuilder carousel(ArrayList bannerUrl, ArrayList redirectUrl){
        if(bannerUrl.size() == redirectUrl.size()){
            for(int i = 0; i < bannerUrl.size(); i++){

                carousel += "\"frame:"+(i+1)+":banner\":\""+bannerUrl.get(i)+"\"," +
                        "\"frame:"+(i+1)+":url\":\""+redirectUrl.get(i)+"\",";
            }

        }

//        this.carousel = new Carousel(bannerUrl, redirectUrl).processCarousel();
        return this;
    }

    public void build(){

        String selfToString = selfToString();
//        String selfToString = mtoString();
        Bundle notificationBundle = jsonStringToBundle(selfToString);
        new SmartpushNotificationManager(mContext).createNotification(notificationBundle);

        try {
            JSONObject j1 = toJsonObject(selfToString);
            Log.d("EXTRA_LOG", j1.toString());
            JSONObject j2 = toJsonObject(mtoString());
            Log.d("EXTRA_LOG", j2.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    String selfToString() {

        return "{" +
                "title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                ", banner='" + banner + '\'' +
                ", url='" + url + '\'' +
                ", video='" + video + '\'' +
                '}';
    }

    public String mtoString() {
        return "{" +
                "title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                ", banner='" + banner + '\'' +
                ", url='" + url + '\'' +
                ", video='" + video + '\'' +
                ", type='" + "CARROUSSEL" + '\'' +
                ", extra=" + "{\n" +
                "        \"frame:1:banner\": \"https://movietvtechgeeks.com/wp-content/uploads/2017/06/xbox-one-vs-ps4-long-battle-images.jpg\",\n" +
                "        \"frame:1:url\": \"buscape://search?productId=27062&site_origem=23708552\",\n" +
                "        \"frame:2:banner\": \"https://i.pinimg.com/originals/fe/63/26/fe6326895705f9f34f250fe274ca9bf3.png\",\n" +
                "        \"frame:2:url\": \"buscape://search?productId=606585&utm_source=alertadepreco&utm_medium=push&utm_campaign=606585\"\n" +
/*                "        \"frame:3:banner\": \"https://www.digiseller.ru/preview/115936/p1_2179893_ef9d38d0.jpg\",\n" +
                "        \"frame:3:url\": \"buscape://search?productId=623321&utm_source=alertadepreco&utm_medium=push&utm_campaign=623321\",\n" +
                "        \"frame:4:banner\": \"https://www.digiseller.ru/preview/115936/p1_2179893_ef9d38d0.jpg\",\n" +
                "        \"frame:4:url\": \"buscape://search?productId=623321&utm_source=alertadepreco&utm_medium=push&utm_campaign=623321\",\n" +
                "        \"frame:5:banner\": \"https://www.digiseller.ru/preview/115936/p1_2179893_ef9d38d0.jpg\",\n" +
                "        \"frame:5:url\": \"buscape://search?productId=623321&utm_source=alertadepreco&utm_medium=push&utm_campaign=623321\"\n" +*/
                "      }\n" +
                '}';

        /*return "{" +
                "title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                ", banner='" + banner + '\'' +
                ", url='" + url + '\'' +
                ", video='" + video + '\'' +
                ", type='" + "CARROUSSEL" + '\'' +
                ", " + "{\n" +
                "        \"frame:1:banner\": \"https://movietvtechgeeks.com/wp-content/uploads/2017/06/xbox-one-vs-ps4-long-battle-images.jpg\",\n" +
                "        \"frame:1:url\": \"buscape://search?productId=27062&site_origem=23708552\",\n" +
                "        \"frame:2:banner\": \"https://i.pinimg.com/originals/fe/63/26/fe6326895705f9f34f250fe274ca9bf3.png\",\n" +
                "        \"frame:2:url\": \"buscape://search?productId=606585&utm_source=alertadepreco&utm_medium=push&utm_campaign=606585\",\n" +
                "        \"frame:3:banner\": \"https://www.digiseller.ru/preview/115936/p1_2179893_ef9d38d0.jpg\",\n" +
                "        \"frame:3:url\": \"buscape://search?productId=623321&utm_source=alertadepreco&utm_medium=push&utm_campaign=623321\",\n" +
                "        \"frame:4:banner\": \"https://www.digiseller.ru/preview/115936/p1_2179893_ef9d38d0.jpg\",\n" +
                "        \"frame:4:url\": \"buscape://search?productId=623321&utm_source=alertadepreco&utm_medium=push&utm_campaign=623321\",\n" +
                "        \"frame:5:banner\": \"https://www.digiseller.ru/preview/115936/p1_2179893_ef9d38d0.jpg\",\n" +
                "        \"frame:5:url\": \"buscape://search?productId=623321&utm_source=alertadepreco&utm_medium=push&utm_campaign=623321\"\n" +
                "      }\n" +
                '}';*/
    }

    public void goGetmo(int i){
        String s = "{\n" +
                "  \"notifications\": [\n" +
                "    {\n" +
                "      \"params\": {\n" +
                "        \"provider\": \"smartpush\",\n" +
                "        \"type\": \"CARROUSSEL\",\n" +
                "        \"title\": \"Buscape\",\n" +
                "        \"detail\": \"Escolhemos ofertas especiais para voce!\",\n" +
                "        \"banner\": \"https://movietvtechgeeks.com/wp-content/uploads/2017/06/xbox-one-vs-ps4-long-battle-images.jpg\",\n" +
                "        \"url\": \"buscape://search?productId=27062&site_origem=23708552\"\n" +
                "      },\n" +
                "      \"extra\": {\n" +
                "        \"frame:1:banner\": \"https://movietvtechgeeks.com/wp-content/uploads/2017/06/xbox-one-vs-ps4-long-battle-images.jpg\",\n" +
                "        \"frame:1:url\": \"buscape://search?productId=27062&site_origem=23708552\",\n" +
                "        \"frame:2:banner\": \"https://i.pinimg.com/originals/fe/63/26/fe6326895705f9f34f250fe274ca9bf3.png\",\n" +
                "        \"frame:2:url\": \"buscape://search?productId=606585&utm_source=alertadepreco&utm_medium=push&utm_campaign=606585\",\n" +
                "        \"frame:3:banner\": \"https://www.digiseller.ru/preview/115936/p1_2179893_ef9d38d0.jpg\",\n" +
                "        \"frame:3:url\": \"buscape://search?productId=623321&utm_source=alertadepreco&utm_medium=push&utm_campaign=623321\",\n" +
                "        \"frame:4:banner\": \"https://www.digiseller.ru/preview/115936/p1_2179893_ef9d38d0.jpg\",\n" +
                "        \"frame:4:url\": \"buscape://search?productId=623321&utm_source=alertadepreco&utm_medium=push&utm_campaign=623321\",\n" +
                "        \"frame:5:banner\": \"https://www.digiseller.ru/preview/115936/p1_2179893_ef9d38d0.jpg\",\n" +
                "        \"frame:5:url\": \"buscape://search?productId=623321&utm_source=alertadepreco&utm_medium=push&utm_campaign=623321\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String s1 = "{\n" +
                "      \"params\": {\n" +
                "        \"type\": \"PUSH\",\n" +
                "        \"provider\": \"smartpush\",\n" +
                "        \"title\": \"Go Getmo\",\n" +
                "        \"detail\": \"Notificações que engajam!\",\n" +
//                "        \"banner\": \"https://pplware.sapo.pt/wp-content/uploads/2018/07/navigation-go.jpg\",\n" +
                "        \"url\": \"getmo://home\",\n" +
                "        \"video\": \"lW4pUQdRo3g\"\n" +
                "      },\n" +
                "      \"extra\": []\n" +
                "    }";

        String s2 = "{\n" +
                "  \"notifications\": [\n" +
                "    {\n" +
                "      \"params\": {\n" +
                "        \"type\": \"PUSH\",\n" +
                "        \"provider\": \"smartpush\",\n" +
                "        \"title\": \"Go Getmo\",\n" +
                "        \"detail\": \"Notificações que engajam!\",\n" +
                "        \"url\": \"getmo://home\",\n" +
                "        \"video\": \"lW4pUQdRo3g\"\n" +
                "      },\n" +
                "      \"extra\": []\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Bundle b = null;

        if(i == 0){
            b = jsonStringToBundle(s);
        } else if (i==1){
            b = jsonStringToBundle(s1);
        } else {
            b = jsonStringToBundle(s2);
        }

        new SmartpushNotificationManager(mContext).createNotification(b);
    }

    private class Carousel{

        private ArrayList bannerUrl, redirectUrl;
        private ArrayList processedCarousel = new ArrayList<String>();

        public Carousel(ArrayList bannerUrl, ArrayList redirectUrl) {
            this.bannerUrl = bannerUrl;
            this.redirectUrl = redirectUrl;
        }

        public ArrayList getBannerUrl() {
            return bannerUrl;
        }

        public ArrayList getRedirectUrl() {
            return redirectUrl;
        }

        @Override
        public String toString() {
            return "Carousel{" +
                    "bannerUrl=" + bannerUrl +
                    ", redirectUrl=" + redirectUrl +
                    '}';
        }

        public String processCarousel(){

            int bannerQuantity = bannerUrl.size();
            int redirectQuantity = redirectUrl.size();

            if(bannerQuantity == redirectQuantity){
                for(int i = 0; i < bannerQuantity; i++){
                    processedCarousel.add(bannerUrl.get(i));
                    processedCarousel.add(redirectUrl.get(i));
                }
            } else {
                for(int i = 0; i < redirectQuantity; i++){
                    processedCarousel.add(bannerUrl.get(i));
                    processedCarousel.add(redirectUrl.get(i));
                }
                int remainQuantity = bannerQuantity - redirectQuantity;
                if(remainQuantity > 0){
                    for(int i = redirectQuantity; i < bannerQuantity; i++){
                        processedCarousel.add(bannerUrl.get(i));
                    }
                }
            }

            return "{ " + processedCarousel + "}";


        }
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
}