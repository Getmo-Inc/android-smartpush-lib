package br.com.smartpush;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;

/**
 * Check device's network connectivity and speed 
 * https://gist.github.com/emil2k/5130324
 **/

class SmartpushConnectivityUtil {

    /**
     * Get the network info
     * @param context Application Context
     * @return NetworkInfo object
     */
    private static NetworkInfo getNetworkInfo( @NonNull Context context ){
        ConnectivityManager cm =
                ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );

        return ( cm != null ) ? cm.getActiveNetworkInfo() : null;
    }

    /**
	 * Check if there is any connectivity
	 * @param context Application Context
	 * @return connection state as boolean
	 */
	public static boolean isConnected( @NonNull Context context ){
	    NetworkInfo info = getNetworkInfo( context );
	    return ( info != null && info.isConnected() );
	}

	/**
	 * Check if there is any connectivity to a Wifi network
	 * @param context Application Context
	 * @return wifi connection as boolean
	 */
	public static boolean isConnectedWifi( @NonNull Context context ){
	    NetworkInfo info = getNetworkInfo(context);
	    return ( info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI );
	}
	
	/**
	 * Check if there is any connectivity to a mobile network
	 * @param context Application Context
	 * @return mobile connection as boolean
	 */
	public static boolean isConnectedMobile( @NonNull Context context ){
	    NetworkInfo info = getNetworkInfo( context );
	    return ( info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE );
	}
	
	/**
	 * Check if there is fast connectivity
	 * @param context Application Context
	 * @return fast connection as boolean
	 */
	public static boolean isConnectedFast( @NonNull Context context){
	    NetworkInfo info = getNetworkInfo( context );
	    return ( info != null
				&& info.isConnected()
				&& SmartpushConnectivityUtil.isConnectionFast( info.getType(), info.getSubtype() ) );
	}
	
	/**
	 * Check if the connection is fast
	 * @param type
	 * @param subType
	 * @return
	 */
	public static boolean isConnectionFast( int type, int subType ) {
		if( type == ConnectivityManager.TYPE_WIFI ){
			return true;
		} else if( type == ConnectivityManager.TYPE_MOBILE ) {
			switch( subType ) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:  // ~ 50-100 kbps
				case TelephonyManager.NETWORK_TYPE_CDMA:   // ~ 14-64 kbps
				case TelephonyManager.NETWORK_TYPE_EDGE:   // ~ 50-100 kbps
				case TelephonyManager.NETWORK_TYPE_GPRS:   // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_IDEN:   // ~25 kbps     :: API level 8
					return false;
				case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
				case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400 kbps
				case TelephonyManager.NETWORK_TYPE_HSDPA:  // ~ 2-14 Mbps
				case TelephonyManager.NETWORK_TYPE_HSPA:   // ~ 700-1700 kbps
				case TelephonyManager.NETWORK_TYPE_HSUPA:  // ~ 1-23 Mbps
				case TelephonyManager.NETWORK_TYPE_UMTS:   // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_EHRPD:  // ~ 1-2 Mbps   :: API level 11
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // ~ 5 Mbps     :: API level 9
                case TelephonyManager.NETWORK_TYPE_HSPAP:  // ~ 10-20 Mbps :: API level 13
                case TelephonyManager.NETWORK_TYPE_LTE:    // ~ 10+ Mbps   :: API level 11
                	return true;
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				default:
					return false;
			}
		}

        return false;
	}
}