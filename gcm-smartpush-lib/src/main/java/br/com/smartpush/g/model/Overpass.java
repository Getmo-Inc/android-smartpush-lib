package br.com.smartpush.g.model;

import br.com.smartpush.u.SmartpushLog;
import br.com.smartpush.u.SmartpushUtils;

/**
 * Created by fabio.licks on 09/02/2016.
 */
public class Overpass {

    public String alias;
    public double lat;
    public double lng;

    public Overpass() {}

    public Overpass( Geozone geozone ) {
        alias = geozone.alias;
        lat = geozone.lat;
        lng = geozone.lng;

        SmartpushLog.getInstance( null ).d( SmartpushUtils.TAG, toString() );
    }

    @Override
    public String toString() {
        return "{ \"alias\":\"" + alias + "\"" +
                ", \"lat\":" + lat +
                ", \"lng\":" + lng + '}';
    }
}