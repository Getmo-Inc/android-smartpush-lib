package br.com.smartpush;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by fabio.licks on 17/07/17.
 */

final class Utils {
    public final static String TAG = "LOG";

    interface Constants {
        // App Metadata
        String SMARTP_APP_ID = "br.com.smartpush.APPID";
        String SMARTP_API_KEY = "br.com.smartpush.APIKEY";
        String SMARTP_PROXY = "br.com.smartpush.PROXY";
        String SMARTP_LOCATION_HASH = "br.com.smartpush.LOCATION_HASH";
        String SMARTP_LOCATIONUPDT = "br.com.smartpush.LOCATIONUPDT";
        String SMARTP_LOCATIONUPDT_IMMEDIATELY = "IMMEDIATELY";
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
        String NOTIF_AUTO_CANCEL = "ac";
        String NOTIF_VIBRATE     = "vib";
        String NOTIF_PACKAGENAME = "package";
        String NOTIF_CATEGORY    = "category";
        String NOTIF_BANNER      = "banner";
        String LAUNCH_ICON       = "icon";

        // Se o array de icones for alterado tem de ajustar o indice desta variavel.
        int NOTIF_CATEGORY_BUSCAPE = 18;
        int PUSH_INTERNAL_ID = 427738108;

        int[] PUSH_DEFAULT_ICONS = {
                R.drawable.ic_esporte,
                R.drawable.ic_cultura,
                R.drawable.ic_turismo,
                R.drawable.ic_noticias,
                R.drawable.ic_imoveis,
                R.drawable.ic_veiculos,
                R.drawable.ic_refeicoes,
                R.drawable.ic_vestuario,         // vestuario
                R.drawable.stat_notify_weather,  // outros
                R.drawable.ic_celular_tablets,
                R.drawable.ic_eletro_info,
                R.drawable.ic_eventos,
                R.drawable.ic_empregos_negocios,
                R.drawable.ic_promocoes,
                R.drawable.ic_bebes_criancas,
                R.drawable.ic_casa_jardim,
                R.drawable.ic_animais,
                R.drawable.ic_sp_notif_buscape  // BUSCAPE
        };

    }

    //=============================================================================================
    static class PreferenceUtils {
        private final static String APP_PREFERENCES = "br.com.smartpush.PREFS";

        public static String readFromPreferences(Context context, String key ) {
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

        public static ArrayList<String> getInstalledApps( Context _c ) {
            PackageManager packageManager = _c.getPackageManager();
            List<ApplicationInfo> list =
                    checkForLaunchIntent( packageManager,
                            packageManager.getInstalledApplications( PackageManager.GET_META_DATA ) );

            ArrayList<String> out = new ArrayList<>();
            for ( ApplicationInfo info : list ) {
                if ( ( info.flags & ApplicationInfo.FLAG_SYSTEM ) == 1 ) {
                    // System application just ignore!
                    continue;
                } else {
                    // Application installed by user
                    //			info.loadLabel(packageManager)
                    //			info.packageName;
                    //			info.loadIcon(packageManager)

                    out.add( info.packageName );
                }
            }

            // return ( new JSONArray( out ) ).toString();
            return out;
        }

        private static List<ApplicationInfo> checkForLaunchIntent( PackageManager packageManager, List<ApplicationInfo> list ) {
            ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
            for ( ApplicationInfo info : list ) {
                try {
                    if ( null != packageManager.getLaunchIntentForPackage( info.packageName ) ) {
                        applist.add( info );
                    }
                } catch ( Exception e ) {
                    SmartpushLog.e( TAG, e.getMessage(), e );
                }
            }

            return applist;
        }

        public static boolean hasPermissions( Context context, String... permissions ) {
            if ( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null ) {
                for ( String permission : permissions ) {
                    if ( ActivityCompat.checkSelfPermission( context, permission ) != PackageManager.PERMISSION_GRANTED ) {
                        return false;
                    }
                }
            }
            return true;
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
    }
}
