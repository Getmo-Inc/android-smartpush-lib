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
    private String carousel = ", \"push.extras\":{";
    private Boolean hasCarousel = false;

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

    public void build(){

        String selfToString = selfToString();
        Bundle notificationBundle = jsonStringToBundle(selfToString);
        Log.d("EXTRA_LOG", notificationBundle.toString());
        new SmartpushNotificationManager(mContext).createNotification(notificationBundle);

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
}