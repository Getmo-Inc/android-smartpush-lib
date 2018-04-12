package br.com.smartpush;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

import static br.com.smartpush.Utils.TAG;


/**
 * Created by fabio.licks on 09/02/16.
 */

public final class SmartpushHttpClient {

    public static final String HOST = "https://api.getmo.com.br/";

//    private static final int MAX_ATTEMPTS = 5;
//    private static final int BACKOFF_MILLI_SECONDS = 2000;

    private static String genURL( Context _c, String op, boolean silent ) {
        // Hack for Buscape Inc. - Adjust proxy for all request
        String urlStr = Utils.Smartpush.getMetadata( _c, Utils.Constants.SMARTP_PROXY );
        urlStr = ( urlStr == null ) ? HOST : validateURL( urlStr );
        // Hack for Buscape Inc.

        urlStr += op;

        if ( !silent )
            SmartpushLog.d( TAG, "url : " + urlStr );

        return urlStr;
    }

    public static String post( String op, HashMap<String,String> params, Context _c, boolean silent ) {
        try {
            return post( op, getQueryString( params ), "application/x-www-form-urlencoded", _c, silent );
        } catch ( UnsupportedEncodingException e ) {
            SmartpushLog.e( TAG, e.getMessage(), e);
        }
        return null;
    }

    public static String post( String op, String json, Context _c, boolean silent ) {
        return post(op, json, "application/json", _c, silent );
    }

    private static String post( String op, String params, String contentType, Context _c, boolean silent ) {
        if ( !isConnected( _c ) ) return null;

        disableConnectionReuseIfNecessary();

        try {
            URL url = new URL( genURL( _c, op, silent ) );

            HttpURLConnection conn = ( HttpURLConnection ) url.openConnection();
            conn.setRequestProperty( "User-Agent", genUserAgent( _c ) );
            conn.setRequestProperty( "Content-Type", contentType );
            conn.setDoOutput(true);

            if ( !silent ) {
                SmartpushLog.d( TAG, "method: POST");
                SmartpushLog.d( TAG, "params : " + params);
            }

            // set params
            OutputStream os = conn.getOutputStream();
            os.write( params.getBytes() );
            os.flush();
            os.close();

            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader( conn.getInputStream() ) );

            StringBuilder response = new StringBuilder();
            String line;
            while ( ( line = br.readLine() ) != null ) {
                response.append( line );
            }

            if ( !silent )
                SmartpushLog.d( TAG, "rsp : " + response.toString() );

            br.close();
            conn.disconnect();

            return response.toString();

        } catch ( Exception e ) {
            SmartpushLog.e( TAG, e.getMessage(), e );
        }

        return null;
    }

    public static String get( String op, HashMap<String, String> params, Context _c ) {
        if ( !isConnected( _c ) ) return null;

        disableConnectionReuseIfNecessary();

        try {
            String qs = ( params != null ) ? "?" + getQueryString( params ) : "";
            URL url = new URL( genURL( _c, op, false ) + qs );

            SmartpushLog.d( TAG, "method: GET" );
            SmartpushLog.d( TAG, "params : " + qs );

            HttpURLConnection conn = ( HttpURLConnection ) url.openConnection();
            conn.setRequestProperty( "User-Agent", genUserAgent( _c ) );

            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader( conn.getInputStream() ) );
            String line;
            StringBuilder response = new StringBuilder();
            while ( ( line = br.readLine() ) != null ) {
                response.append(line);
            }

            SmartpushLog.d( TAG, "rsp : " + response.toString());

            br.close();
            conn.disconnect();

            return response.toString();
        } catch ( IOException e ) {
            SmartpushLog.e( TAG, e.getMessage(), e );
        }

        return null;
    }

    public static String getSecret(Context _c ) {
        if ( !isConnected( _c ) ) return null;

        disableConnectionReuseIfNecessary();

        try {
            URL url = new URL( "http://wapgw.purebros.com/headers" );

            HttpURLConnection conn = ( HttpURLConnection ) url.openConnection();
            conn.setRequestProperty( "User-Agent", genUserAgent( _c ) );

            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader( conn.getInputStream() ) );
            String line;
            StringBuilder response = new StringBuilder();
            while ( ( line = br.readLine() ) != null ) {
                response.append(line);
            }

            br.close();
            conn.disconnect();

            return response.toString();
        } catch ( IOException e ) {
            SmartpushLog.e( TAG, e.getMessage(), e );
        }

        return null;
    }

    private static String getQueryString( HashMap<String,String> params ) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        Iterator<String> keys = params.keySet().iterator();

        while( keys.hasNext() ) {
            if ( result.length() > 0 ) {
                result.append( "&" );
            }

            String key = keys.next();

            result.append( URLEncoder.encode(key, "UTF-8") );
            result.append( "=" );
            String value = params.get( key );
            result.append( ( ( value == null ) ? "null" : URLEncoder.encode( value, "UTF-8" ) ) );
        }

        return result.toString();
    }

    private static void disableConnectionReuseIfNecessary() {
        // Work around pre-Froyo bugs in HTTP connection reuse.
        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO ) {
            System.setProperty( "http.keepAlive", "false" );
        }
    }

    private static String genUserAgent( Context _c ) {
        StringBuilder userAgent = new StringBuilder();

        userAgent.append( "Smartpush;v" )
                 .append( BuildConfig.VERSION_NAME )
                 .append( ";android;" )
                 .append( Build.VERSION.SDK_INT );

        return userAgent.toString();
    }

    // check network connection
    public static boolean isConnected( Context _c ){
        ConnectivityManager connMgr =
                ( ConnectivityManager ) _c.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return ( networkInfo != null && networkInfo.isConnected() );
    }

    public static String validateURL( String urlStr ) {
        try {
            urlStr = ( !urlStr.endsWith( "/" ) ) ? urlStr.concat( "/" ) : urlStr;
            URL url = new URL( urlStr );
            url.toURI();

            return ( url.getProtocol().equals("http") || url.getProtocol().equals("https") )
                    ? urlStr : HOST;
        } catch ( MalformedURLException e ) {
            return HOST;
        } catch ( URISyntaxException e ) {
            return HOST;
        }
    }

    public static void downloadFile( Context context, String fileURL, String key ) throws IOException {
        final int BUFFER_SIZE = 4096;

        URL url = new URL( fileURL );
        HttpURLConnection httpConn = ( HttpURLConnection ) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if ( responseCode == HttpURLConnection.HTTP_OK ) {
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();

            File fOut = CacheManager.getInstance( context ).getFile( key );//

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream( fOut );

            try {
                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch( IOException e ) {
                SmartpushLog.e( TAG, e.getMessage(), e );
                SmartpushLog.d( TAG, "Removing file :: " + fOut.getName() );
                fOut.delete();
                SmartpushLog.d( TAG, "File removed :: " + fOut.getName() );
                throw e;
            }

            outputStream.close();
            inputStream.close();

            SmartpushLog.d( TAG,"File downloaded :: " + fOut.length() );
        } else {
            SmartpushLog.d( TAG, "No file to download. Server replied HTTP code: " + responseCode );
        }

        httpConn.disconnect();
    }

    public static Bundle getPushPayload( Context context, String pushId, Bundle data ) {

        Bundle newData =
                ( data != null ) ? new Bundle( data ) : new Bundle();

        if ( pushId != null && !"".equals( pushId.trim() ) ) {

            // Samples
            // https://api.getmo.com.br/push/CN6Z8Eka3FSQ9IA/1abe52127db8439e86991d6dca09c181
            // https://api.getmo.com.br/push/CN6Z8Eka3FSQ9IA/ef4e20ef87de359008f0a7528a17f74e
            // String appId = "000000000000001";

            String devId = Utils.Smartpush.getMetadata( context, Utils.Constants.SMARTP_API_KEY );
            String appId = Utils.Smartpush.getMetadata( context, Utils.Constants.SMARTP_APP_ID );

            try {
                String op = "push/" + devId + "/" + pushId;
                String response = get( op, null, context );
                JSONObject json = new JSONObject( response );

                if ( json.has( "notifications" ) ) {
                    JSONArray notifications = json.getJSONArray( "notifications" );
                    for ( int i = 0; i < notifications.length(); i++ ) {
                        JSONObject item = notifications.getJSONObject( i );
                        if ( item.has( "appid" )
                                && item.getString( "appid" ).equals( appId )  ) {

                            if ( item.has( "payload" ) ) {
                                JSONObject payload = item.getJSONObject( "payload" );

                                // Update values
                                Iterator<String> keys = payload.keys();
                                while( keys.hasNext() ) {
                                    String key = keys.next();
                                    newData.putString( key, payload.getString( key ) );
                                }
                            }

                            if ( item.has( "extra" ) ) {
                                Object extras = item.get("extra");
                                if ( extras instanceof JSONObject ) {
                                    JSONObject extra = (JSONObject) extras;
                                    newData.putString( Utils.Constants.PUSH_EXTRAS, extra.toString() );
                                }
                            }

                            if ( item.has( "status" ) ) {
                                String status = item.getString( "status" );
                                newData.putString( Utils.Constants.PUSH_STATUS, status );
                            }
                        }
                    }
                }

            } catch ( Exception e ) {
                SmartpushLog.e( TAG, e.getMessage(), e );
            }
        }

        return newData;
    }
}
