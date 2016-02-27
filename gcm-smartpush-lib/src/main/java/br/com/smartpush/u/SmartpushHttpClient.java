package br.com.smartpush.u;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
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

import br.com.smartpush.R;

/**
 * Created by fabio.licks on 09/02/16.
 */

public class SmartpushHttpClient {

    public static final String HOST = "http://api.getmo.com.br/";

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;

    private static String genURL( Context _c, String op ) {
        // Hack for Buscape Inc. - Adjust proxy for all request
        String urlStr = SmartpushUtils.getSmartPushMetadata(_c, SmartpushUtils.SMARTP_PROXY);
        urlStr = ( urlStr == null ) ? HOST : validateURL( urlStr );
        // Hack for Buscape Inc.

        urlStr += op;

        Log.d( SmartpushUtils.TAG, "url : " + urlStr );

        return urlStr;
    }

    public static String post( String op, HashMap<String,String> params, Context _c ) {
        try {
            return post( op, getQueryString( params ), "application/x-www-form-urlencoded", _c);
        } catch ( UnsupportedEncodingException e ) {
            Log.e( SmartpushUtils.TAG, e.getMessage(), e);
        }
        return null;
    }

    public static String post( String op, String json, Context _c ) {
        return post(op, json, "application/json", _c);
    }

    private static String post( String op, String params, String contentType, Context _c ) {
        if ( !isConnected( _c ) ) return null;

        disableConnectionReuseIfNecessary();

        try {
            URL url = new URL( genURL( _c, op ) );

            HttpURLConnection conn = ( HttpURLConnection ) url.openConnection();
            conn.setRequestProperty( "User-Agent", genUserAgent( _c ) );
            conn.setRequestProperty( "Content-Type", contentType );
            conn.setDoOutput(true);

            Log.d( SmartpushUtils.TAG, "method: POST" );
            Log.d( SmartpushUtils.TAG, "params : " + params );

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

            Log.d( SmartpushUtils.TAG, "rsp : " + response.toString() );

            br.close();
            conn.disconnect();

            return response.toString();

        } catch ( Exception e ) {
            Log.e( SmartpushUtils.TAG, e.getMessage(), e );
        }

        return null;
    }

    public static String get( String op, HashMap<String, String> params, Context _c ) {
        if ( !isConnected( _c ) ) return null;

        disableConnectionReuseIfNecessary();

        try {
            String qs = ( params != null ) ? "?" + getQueryString( params ) : "";
            URL url = new URL( genURL( _c, op ) + qs );

            Log.d( SmartpushUtils.TAG, "method: GET" );
            Log.d( SmartpushUtils.TAG, "params : " + qs );

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

            Log.d(SmartpushUtils.TAG, "rsp : " + response.toString());

            br.close();
            conn.disconnect();

            return response.toString();
        } catch ( IOException e ) {
            Log.e( SmartpushUtils.TAG, e.getMessage(), e );
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

        userAgent.append( "Smartpush;sdk_v;" )
                 .append( _c.getString( R.string.smartp_version ) )
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

    public static Bitmap loadBitmap( String urlpath ) throws MalformedURLException, IOException {
//		BitmapDrawable bitmapDrawable =
//          new BitmapDrawable( BitmapFactory.decodeStream( new URL( url ).openStream() ) );
//		return bitmapDrawable.getBitmap();
        if ( urlpath == null ) return null;

        URL url = new URL( urlpath );
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput( true );
        connection.connect();
        return BitmapFactory.decodeStream(connection.getInputStream());
    }
}
