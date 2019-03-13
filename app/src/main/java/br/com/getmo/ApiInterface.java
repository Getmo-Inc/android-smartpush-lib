package br.com.getmo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

interface ApiInterface {

//    @Headers({"Content-Type:application/json"})
//    @POST("/push")
//    Call<String> sendPushNotification( @Body String json );

    @FormUrlEncoded
    @POST("/push")
    Call<String> sendPushNotification( @Field("data") String data );
}
