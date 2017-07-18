package br.com.smartpush;


/**
 * Created by fabio.licks on 09/02/2016.
 */
final class GeoOverpass {

    public String alias;
    public double lat;
    public double lng;

    public GeoOverpass() {}

    public GeoOverpass( Geozone geozone ) {
        alias = geozone.alias;
        lat = geozone.lat;
        lng = geozone.lng;

        SmartpushLog.d( Utils.TAG, toString() );
    }

    @Override
    public String toString() {
        return "{ \"alias\":\"" + alias + "\"" + ", \"lat\":" + lat + ", \"lng\":" + lng + '}';
    }
}