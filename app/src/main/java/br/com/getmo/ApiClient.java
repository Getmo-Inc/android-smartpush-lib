package br.com.getmo;

import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static final String TAG = "LOG";

    static Retrofit getClient(){

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor( new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain ) throws IOException {
                Log.d( TAG, chain.request().url().toString() );

                return chain.proceed( chain.request() );
            }
        });

        httpClient.addInterceptor( new Interceptor() {
            @Override
            public okhttp3.Response intercept( Chain chain ) throws IOException {
                try {
                    Response response = chain.proceed( chain.request() );
                    Log.e( TAG, "response code: " + response.code() );

                    if ( !response.isSuccessful() ) {
                        Log.d( TAG, "central server error handling");

                        // Central error handling for error responses here:
                        // e.g. 4XX and 5XX errors
                        switch ( response.code() ) {
                            case 401:
                                // do something when 401 Unauthorized happened
                                // e.g. delete credentials and forward to login screen
                                // ...

                                break;
                            case 403:
                                // do something when 403 Forbidden happened
                                // e.g. delete credentials and forward to login screen
                                // ...

                                break;
                            case 500:
                                // do something when 403 Forbidden happened
                                // e.g. delete credentials and forward to login screen
                                // ...

                                try {
                                    JSONObject object = new JSONObject( response.body().string() );
                                    String error = ":(";

                                    if ( object.has("ExceptionMessage" ) ) {
                                        error = object.getString( "ExceptionMessage" );
                                    }

                                    throw new IOException( error );
                                } catch ( JSONException e ) {
                                    Log.e( TAG, e.getMessage() );
                                }
                                break;
                            default:
                                Log.e( TAG, "Log error or do something else with error code:" + response.code());

                                break;
                        }
                    }

                    return response;
                } catch ( IOException e ) {
                    // Central error handling for network errors here:
                    // e.g. no connection to internet / to server

                    Log.e( TAG, e.getMessage(), e );
                    Log.e( TAG, "central network error handling");

                    throw e;
                }
            }
        });

        httpClient.addInterceptor(
                new HttpLoggingInterceptor()
                        .setLevel( HttpLoggingInterceptor.Level.BODY ) );

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.getmo.com.br")
                .addConverterFactory(GsonConverterFactory.create())
                .client( httpClient.build() )
                .build();


        return retrofit;
    }

}
