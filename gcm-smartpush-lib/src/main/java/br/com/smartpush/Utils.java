package br.com.smartpush;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static br.com.smartpush.Utils.Constants.NOTIF_VIDEO_URI;
import static br.com.smartpush.Utils.Constants.ONLY_PORTRAIT;
import static br.com.smartpush.Utils.Constants.OPEN_IN_BROWSER;

/**
 * Created by fabio.licks on 17/07/17.
 */

final public class Utils {
    public final static String TAG = "LOG";

    public interface Constants {
        // App Metadata
        String SMARTP_APP_ID = "br.com.smartpush.APPID";
        String SMARTP_API_KEY = "br.com.smartpush.APIKEY";
        String SMARTP_PROXY = "br.com.smartpush.PROXY";
        String SMARTP_LOCATION_HASH = "br.com.smartpush.LOCATION_HASH";
        String SMARTP_LOCATIONUPDT = "br.com.smartpush.LOCATIONUPDT";
        String SMARTP_LOCATIONUPDT_IMMEDIATELY = "IMMEDIATELY";
        String SMARTP_SMALL_ICON = "br.com.smartpush.default_notification_small_icon";
        String SMARTP_BIG_ICON = "br.com.smartpush.default_notification_big_icon";
        String SMARTP_NOTIFICATION_COLOR = "br.com.smartpush.default_notification_color";
        String SMARTP_HWID = "br.com.smartpush.HWID";
        String SMARTP_ALIAS = "br.com.smartpush.ALIAS";
        String SMARTP_REGID = "br.com.smartpush.REGID";

        String ONLY_PORTRAIT = "br.com.getmo.orientation.policy.PORTRAIT";
        String REDIRECTED = "br.com.getmo.orientation.policy.REDIRECTED";


        // Push/Notification metadata
        String NOTIF_TITLE       = "title";
        String NOTIF_DETAIL      = "detail";
        String NOTIF_URL         = "url";
        String NOTIF_VIDEO_URI   = "video";
        String NOTIF_PLAY_VIDEO_ONLY_WIFI = "play_video_only_on_wifi";
        String OPEN_IN_BROWSER   = "open_url_in_browser";
        String NOTIF_AUTO_CANCEL = "ac";
        String NOTIF_VIBRATE     = "vib";
        String NOTIF_PACKAGENAME = "package";
        String NOTIF_CATEGORY    = "category";
        String NOTIF_BANNER      = "banner";
        String LAUNCH_ICON       = "icon";

        String PUSH_STATUS       = "push.status";
        String PUSH_EXTRAS       = "push.extras";
        String PUSH_UPDATE_COUNT = "push.update.count";

        int PUSH_INTERNAL_ID = 427738108;
    }

    //=============================================================================================
    public static class PreferenceUtils {
        private final static String APP_PREFERENCES = "br.com.smartpush.PREFS";

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
            return context.getSharedPreferences( APP_PREFERENCES, Context.MODE_PRIVATE );
        }
    }

    //=============================================================================================
    static class ArrayUtils<T> {
        public String toJsonArrayString(List<T> list ) {

            if ( list == null ) return "[]";

            StringBuilder sb = new StringBuilder();
            Iterator<T> iterator = list.iterator();

            sb.append("[");
            while ( iterator.hasNext() ) {
                T item = iterator.next();
                sb.append(item.toString());
            }
            sb.append("]");

            return sb.toString();
        }

        public static String bundle2string( Bundle bundle) {
            StringBuffer buf = new StringBuffer( "Bundle{ ");
            for ( String key : bundle.keySet() )
                buf.append( " " + key + " => " + bundle.get(key) + ";");
            buf.append(" }Bundle");
            return buf.toString(); }
    }

    //=============================================================================================
    static class DeviceUtils {
        public static String getLanguage() {
            return Locale.getDefault().toString();
        }

        public static String getDeviceName() {
            return ( Build.MODEL != null ) ? Build.MODEL.toUpperCase() : "";
        }

        public static String getDeviceManufacturer() {
            return ( Build.MANUFACTURER != null ) ? Build.MANUFACTURER.toUpperCase() : "";
        }

        public static boolean hasPermissions( Context context, String... permissions ) {
//            if ( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null ) {
            if ( context != null && permissions != null ) {
                for ( String permission : permissions ) {
                    if ( ContextCompat.checkSelfPermission( context, permission ) != PackageManager.PERMISSION_GRANTED ) {
                        return false;
                    }
                }
                return true;
            }
//
            return false;
        }
    }

    //=============================================================================================
    static class CommonUtils {
        public static String getValue( String val, String defValue ) {
            return ( val != null ) ? val : defValue;
        }
    }

    //=============================================================================================
    static class CryptoUtils {
        public static final String md5( final String s ) {
            try {
                // Create MD5 Hash
                MessageDigest digest = MessageDigest.getInstance( "MD5" );
                digest.update( s.getBytes() );
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
                SmartpushLog.e( TAG, e.getMessage(), e );
            }
            return "";
        }
    }

    static class Smartpush {
        public static String getMetadata( Context _c, String key ) {
            try {
                ApplicationInfo ai =
                        _c.getPackageManager()
                                .getApplicationInfo(_c.getPackageName(), PackageManager.GET_META_DATA);
//                return ( SMARTP_DEBUG.equals( key ) )
//                        ? Boolean.toJsonArrayString(ai.metaData.getBoolean(key))
//                        : ai.metaData.getString(key);
                return ai.metaData.getString(key);
            } catch ( PackageManager.NameNotFoundException e ) {
                SmartpushLog.e(TAG,
                        "Failed to load meta-data, NameNotFound: " + e.getMessage(), e);
            } catch ( NullPointerException e ) {
                SmartpushLog.e(TAG,
                        "Failed to load meta-data, NullPointer: " + e.getMessage(), e);
            }

            return "";
        }

        public static int getResourceIdFromMetadata( Context _c, String key ) {
            try {
                ApplicationInfo ai =
                        _c.getPackageManager()
                                .getApplicationInfo(_c.getPackageName(), PackageManager.GET_META_DATA);
                return ai.metaData.getInt(key, -1);
            } catch ( PackageManager.NameNotFoundException e ) {
                SmartpushLog.e(TAG,
                        "Failed to load meta-data, NameNotFound: " + e.getMessage(), e);
            } catch ( NullPointerException e ) {
                SmartpushLog.e(TAG,
                        "Failed to load meta-data, NullPointer: " + e.getMessage(), e);
            }

            return -1;
        }

        public static Intent getIntentToRedirect( Context context, String url, String packageName, Bundle extras ) {
            Intent intent = null;

            if ( context != null ) {
                if ( packageName != null ) {
                    intent = context.getPackageManager().getLaunchIntentForPackage( packageName );
                    if ( intent != null ) {
                        intent.putExtras( extras );
                    }
                } else if ( extras.containsKey( NOTIF_VIDEO_URI ) ) {
                    intent = new Intent( context, SmartpushActivity.class );
                    intent.putExtras( extras );
                } else if ( url != null && url.contains( "://" )) {
                    boolean openInBrowser = true;

                    if ( extras.containsKey( OPEN_IN_BROWSER ) ) {
                        openInBrowser = extras.getInt( OPEN_IN_BROWSER ) == 0 ? false : true;
                    }

                    if ( !openInBrowser ) {
                        intent = new Intent( context, SmartpushActivity.class );
                        intent.putExtra( ONLY_PORTRAIT, true );
                    } else {
                        intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ));
                    }

                    intent.putExtras( extras );
                }
            }

            if ( intent != null )
                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );

            return intent;
        }
    }
}
