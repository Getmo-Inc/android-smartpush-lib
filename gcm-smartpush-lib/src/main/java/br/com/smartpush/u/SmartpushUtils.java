package br.com.smartpush.u;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Created by fabio.licks on 09/02/16.
 */

public class SmartpushUtils {

    public final static String TAG = "SMARTPUSH_LOG";

    public final static String APP_PREFERENCES = "br.com.smartpush.PREFS";

    // App Metadata
    public static final String SMARTP_APP_ID = "br.com.smartpush.APPID";
    public static final String SMARTP_API_KEY = "br.com.smartpush.APIKEY";
    public static final String SMARTP_PROXY = "br.com.smartpush.PROXY";
    public static final String SMARTP_DEBUG = "br.com.smartpush.DEBUG";
    public static final String SMARTP_LOCATION_HASH = "br.com.smartpush.LOCATION_HASH";
    public static final String SMARTP_LOCATIONUPDT = "br.com.smartpush.LOCATIONUPDT";
    public static final String SMARTP_LOCATIONUPDT_IMMEDIATELY = "IMMEDIATELY";
    public static final String SMARTP_HWID = "br.com.smartpush.HWID";
    public static final String SMARTP_ALIAS = "br.com.smartpush.ALIAS";
    public static final String SMARTP_REGID = "br.com.smartpush.REGID";

    public static String ONLY_PORTRAIT = "br.com.getmo.orientation.policy.PORTRAIT";
    public static String REDIRECTED = "br.com.getmo.orientation.policy.REDIRECTED";

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            SmartpushLog.getInstance( null ).e(TAG, e.getMessage(), e);
        }
        return "";
    }

    public static String getSmartPushMetadata(Context _c, String key) {
        try {
            ApplicationInfo ai =
                    _c.getPackageManager()
                            .getApplicationInfo(_c.getPackageName(), PackageManager.GET_META_DATA);
            return ( SMARTP_DEBUG.equals( key ) )
                        ? Boolean.toString(ai.metaData.getBoolean(key))
                        : ai.metaData.getString(key);
        } catch ( PackageManager.NameNotFoundException e ) {
            SmartpushLog.getInstance( _c ).e(TAG,
                    "Failed to load meta-data, NameNotFound: " + e.getMessage(), e);
        } catch ( NullPointerException e ) {
            SmartpushLog.getInstance( _c ).e(TAG,
                    "Failed to load meta-data, NullPointer: " + e.getMessage(), e);
        }

        return "";
    }

    public static String readFromPreferences( Context context, String key ) {
        return getSmartpushPreferences( context ).getString( key, null );
    }

    public static String readFromPreferences( Context context, String key, String defaultValue ) {
        defaultValue = ( defaultValue == null ) ? "null" : defaultValue;
        return getSmartpushPreferences( context ).getString( key, defaultValue );
    }

    public static boolean deleteFromPreferences( Context context, String key ) {
        SharedPreferences.Editor edit = getSmartpushPreferences(context ).edit();
        edit.remove( key );
        edit.apply();

        return true;
    }

    public static String saveOnPreferences( Context context, String key, String value ) {
        SharedPreferences.Editor edit = getSmartpushPreferences(context ).edit();
        edit.putString( key, value );
        edit.apply();

        return value;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private static SharedPreferences getSmartpushPreferences( Context context ) {
        return context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static String getLanguage() {
        return Locale.getDefault().toString();
    }

    public static String getDeviceName() {
        return (Build.MODEL != null) ? Build.MODEL.toUpperCase() : "";
    }

    public static String getDeviceManufacturer() {
        return (Build.MANUFACTURER != null) ? Build.MANUFACTURER.toUpperCase() : "";
    }

    public static String getValue( String val, String defValue ) {
        return ( val != null ) ? val : defValue;
    }
}
