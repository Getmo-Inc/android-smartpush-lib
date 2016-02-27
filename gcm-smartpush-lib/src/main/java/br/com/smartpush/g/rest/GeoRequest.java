package br.com.smartpush.g.rest;

import android.content.Context;

import java.util.ArrayList;

import br.com.smartpush.g.model.Address;
import br.com.smartpush.g.model.Location;
import br.com.smartpush.g.model.Overpass;
import br.com.smartpush.u.SmartpushArraysUtil;
import br.com.smartpush.u.SmartpushUtils;

/**
 * Created by leticia on 02/10/15.
 */
public class GeoRequest {

    private String devid;
    private String appid;
    private String hwid;

    private String hash;
    private Address info;
    private ArrayList<Location> locations;

    private Overpass overpass;

    public GeoRequest( Context context, String hash ) {

        devid = SmartpushUtils.getSmartPushMetadata( context, SmartpushUtils.SMARTP_API_KEY );
        appid = SmartpushUtils.getSmartPushMetadata(context, SmartpushUtils.SMARTP_APP_ID);
        hwid  = SmartpushUtils.readFromPreferences( context, SmartpushUtils.SMARTP_HWID );

        locations = new ArrayList<>();

        if ( hash == null || "".equals( hash.trim() ) ) {
            new RuntimeException( "HASH wasn't set!" );
        } else {
            this.hash = hash;
        }
    }

    public String getDevid() {
        return devid;
    }

    public String getAppid() {
        return appid;
    }

    public String getHwid() {
        return hwid;
    }

    public String getHash() {
        return hash;
    }

    public void setHash( String hash ) {
        this.hash = hash;
    }

    public Address getInfo() {
        return info;
    }

    public void setInfo( Address info ) {
        this.info = info;
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public void setLocations( ArrayList<Location> locations ) {
        this.locations = locations;
    }

    public Overpass getOverpass() {
        return overpass;
    }

    public void setOverpass( Overpass overpass ) {
        this.overpass = overpass;
    }

    public String toJSONString() {
        return "{ \"devid\":\"" + devid + "\"" +
                ", \"appid\":\"" + appid + "\"" +
                ", \"hwid\":\"" + hwid + "\"" +
                ", \"hash\":\"" + hash + "\"" +
                ( ( info != null ) ? ", \"info\":" + info.toString() : "" ) +
                ( ( locations != null ) ? ", \"locations\":" + new SmartpushArraysUtil<Location>().toString( locations ) : "" ) +
                ( ( overpass != null ) ? ", \"overpass\":" + overpass.toString() : "" ) + '}';
    }
}
