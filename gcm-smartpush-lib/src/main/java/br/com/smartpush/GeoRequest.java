package br.com.smartpush;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by fabio.licks on 02/10/15.
 */
class GeoRequest {

    private String devid;
    private String appid;
    private String hwid;

    private String hash;
    private GeoAddress info;
    private ArrayList<GeoLocation> locations;

    private GeoOverpass overpass;

    public GeoRequest( Context context, String hash ) {

        devid = Utils.Smartpush.getMetadata( context, Utils.Constants.SMARTP_API_KEY );
        appid = Utils.Smartpush.getMetadata(context, Utils.Constants.SMARTP_APP_ID);
        hwid  = Utils.PreferenceUtils.readFromPreferences( context, Utils.Constants.SMARTP_HWID );

        locations = new ArrayList<>();

        if ( hash == null || "".equals( hash.trim() ) ) {
            new RuntimeException( "HASH wasn't set!" );
        } else {
            this.hash = hash;
        }
    }

//    public String getDevid() {
//        return devid;
//    }
//
//    public String getAppid() {
//        return appid;
//    }
//
//    public String getHwid() {
//        return hwid;
//    }
//
//    public String getHash() {
//        return hash;
//    }
//
//    public void setHash( String hash ) {
//        this.hash = hash;
//    }

    public GeoAddress getInfo() {
        return info;
    }

    public void setInfo( GeoAddress info ) {
        this.info = info;
    }

//    public ArrayList<GeoLocation> getLocations() {
//        return locations;
//    }

    public void setLocations( ArrayList<GeoLocation> locations ) {
        this.locations = locations;
    }

//    public GeoOverpass getOverpass() {
//        return overpass;
//    }

    public void setOverpass( GeoOverpass overpass ) {
        this.overpass = overpass;
    }

    public String toJSONString() {
        return "{ \"devid\":\"" + devid + "\"" +
                ", \"appid\":\"" + appid + "\"" +
                ", \"hwid\":\"" + hwid + "\"" +
                ", \"hash\":\"" + hash + "\"" +
                ( ( info != null ) ? ", \"info\":" + info.toString() : "" ) +
                ( ( locations != null ) ? ", \"locations\":" + new Utils.ArrayUtils<GeoLocation>().toJsonArrayString( locations ) : "" ) +
                ( ( overpass != null ) ? ", \"overpass\":" + overpass.toString() : "" ) + '}';
    }
}
