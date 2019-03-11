package br.com.getmo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

interface ApiInterface {

    @FormUrlEncoded
    @POST("/push")
    Call<String> sendPushNotification(
            @Field("alias") String alias,
            @Field("inbox") Boolean inbox,
            @Field("prod") int prod,
            @Field("devid") String devid,
            @Field("notifications") List<Notif> notif,
            @Field("filter") Filter filter
    );
}
