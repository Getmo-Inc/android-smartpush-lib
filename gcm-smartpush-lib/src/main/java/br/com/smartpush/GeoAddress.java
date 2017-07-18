package br.com.smartpush;


/**
 * Created by fabio.licks on 09/02/16.
 */
final class GeoAddress {

    public String countryCode;
    public String country;
    public String state;
    public String city;
    public String neighborhood;

    public GeoAddress() { }

    public GeoAddress(String countryCode, String country, String state, String city, String neighborhood ) {
        this.countryCode = countryCode;
        this.country = country;
        this.state = state;
        this.city = city;
        this.neighborhood = neighborhood;

        SmartpushLog.d( Utils.TAG, toString() );
    }

    @Override
    public String toString() {
        return "{ \"country_code\":\"" + countryCode + "\"" +
                ", \"country\":\"" + country + "\"" +
                ", \"state\":\"" + state + "\"" +
                ", \"city\":\"" + city + "\"" +
                ", \"neighborhood\":\"" + neighborhood + "\"}";
    }
}
