package br.com.smartpush;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static br.com.smartpush.SmartpushConnectivityUtil.isConnectedWifi;
import static br.com.smartpush.Utils.TAG;

/**
 * Created by fabio.licks on 24/07/17.
 */

public class CacheManager {

    public enum ExpirationTime {
        DAY( 1000 * 60 * 60 * 24 ), NONE( Long.MAX_VALUE );

        long expiration;

        private ExpirationTime( long time ) {
            expiration = time;
        }

        public long getExpirationTime() {
            return expiration;
        }
    }

    private Context mContext;

    private static CacheManager mInstance;

    private CacheManager( Context context ) {
        mContext = context;
    }

    public static CacheManager getInstance( Context context ) {
        if ( mInstance == null ) {
            mInstance = new CacheManager( context );
        }

        return mInstance;
    }

    public Bitmap loadBitmap( String urlpath, ExpirationTime expirationTime ) {
        final String key = CacheManager.generateKey( urlpath );
        Log.d( TAG, "LINK: " + urlpath );
        if ( key != null ) {
            Log.d( TAG, "KEY: " + key );

            if ( isInCache( key, expirationTime ) ) {
                SmartpushLog.d( TAG, "IMAGE IN CACHE..." );
                return CacheManager
                        .bitmapFromFile(
                                getFile( key ).getAbsolutePath() );
            } else {
                Log.d( TAG, "IMAGE NOT IN CACHE..." );
                try {
                    SmartpushLog.d( TAG, "DOWNLOADING IMAGE..." );
                    Bitmap bitmap =
                            new BitmapDrawable( mContext.getResources(),
                                    BitmapFactory.decodeStream( new URL( urlpath ).openStream() ) )
                                    .getBitmap();

                    if ( bitmap != null ) {
                        SmartpushLog.d(TAG, "ADDING IMAGE TO CACHE...");
                        bitmapToFile(bitmap, key);
                    }

                    return bitmap;

                } catch( IOException e ) {
                    Log.e( TAG, e.getMessage(), e );
                    deleteFile( key );
                }
            }
        }

        return null;
    }

    public String prefetchVideo( String midiaId, ExpirationTime expirationTime ) {
        String key = generateKey( midiaId );
        SmartpushLog.d(TAG, "LINK: " + midiaId);
        if ( key != null ) {
            SmartpushLog.d(TAG, "KEY: " + key);

            if ( isInCache( key, expirationTime ) ) {
                SmartpushLog.d(TAG, "VIDEO IN CACHE...");
                // so do nothing!!

            } else {
                SmartpushLog.d(TAG, "VIDEO NOT IN CACHE...");
                String resp = SmartpushHttpClient.get( "play/" + midiaId + "/info", null, mContext );
                try {
                    JSONObject json = new JSONObject(resp);
                    boolean status = json.has("status") ? json.getBoolean("status") : false;

                    if (status) {
                        JSONArray videos = json.getJSONArray("videos");

                        int idSelected = -1;
                        String extension = ( isConnectedWifi( mContext ) ) ? "mp4" : "3gp";

                        int bestSize = ( "mp4".equals( extension ) ) ? Integer.MAX_VALUE : Integer.MIN_VALUE;

                        for (int i = 0; i < videos.length(); i++) {
                            JSONObject o = videos.getJSONObject(i);

                            if (o.getString("extension").equals(extension)) {
                                int currentSize = o.getInt("filesize");

                                if ("3gp".equals(extension)) {
                                    if (bestSize < currentSize) {
                                        bestSize = currentSize;
                                        idSelected = i;
                                    }
                                } else {
                                    if (bestSize > currentSize) {
                                        bestSize = currentSize;
                                        idSelected = i;
                                    }
                                }
                            }
                        }

                        String linkVideo = ( idSelected != -1 )
                                ? videos.getJSONObject(idSelected).getString("url")
                                : null;

                        SmartpushLog.d(TAG, "---> " + linkVideo);
                        SmartpushLog.d(TAG, "---> " + key );

                        if ( linkVideo != null ) {
                            SmartpushHttpClient.downloadFile( mContext, linkVideo, key );
                        }
                    }
                } catch ( Exception e ) {
                    Log.e( TAG, e.getMessage(), e );
                    key = null;
                }
            }
        }

        return key;
    }

    public File getFile( String key ) {
        File dir  = mContext.getCacheDir();
        File file = new File( dir, key );

        return file;
    }

    private boolean deleteFile( String key ) {
        File file = getFile( key );
        return ( file.exists() ) ? file.delete() : false;
    }

    private boolean isInCache( String key, ExpirationTime cacheTime ) {
        File file = getFile( key );
        long expirationTime = cacheTime.getExpirationTime();
        return file.exists() && ( file.lastModified() + expirationTime > System.currentTimeMillis() ) ;
    }

    private void bitmapToFile( Bitmap bitmap, String filename ) throws IOException {
        int quality = 100;

        File file = getFile( filename );
        FileOutputStream fileOutputStream = new FileOutputStream( file );
        bitmap.compress( Bitmap.CompressFormat.PNG, quality, fileOutputStream );
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    private static String generateKey( String input ) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.reset();
            byte[] buffer = input.getBytes("UTF-8");
            md.update(buffer);
            byte[] digest = md.digest();

            String hexStr = "";
            for (int i = 0; i < digest.length; i++) {
                hexStr += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
            }
            return hexStr;
        } catch ( NoSuchAlgorithmException | UnsupportedEncodingException e ) {
            Log.e( TAG, e.getMessage(), e );
        }

        return null;
    }

    private static Bitmap bitmapFromFile( String filePath ) {
        Bitmap bitmap = BitmapFactory.decodeFile( filePath );
        return bitmap;
    }
}