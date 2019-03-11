package br.com.getmo;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;

    static Retrofit getClient(){

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.getmo.com.br")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        return retrofit;
    }

}
