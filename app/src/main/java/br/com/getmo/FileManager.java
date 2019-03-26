package br.com.getmo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileManager {

    // convert InputStream to String
    public static String getStringFromInputStream( InputStream is ) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            br = new BufferedReader( new InputStreamReader( is ) );
            while ( ( line = br.readLine() ) != null ) {
                sb.append(line);
            }
        } catch ( IOException e ) {
            Log.e( "LOG", e.getMessage(), e );
        } finally {
            if ( br != null ) {
                try {
                    br.close();
                } catch ( IOException e ) {
                    Log.e( "LOG", e.getMessage(), e );
                }
            }
        }

        return sb.toString();
    }
}
