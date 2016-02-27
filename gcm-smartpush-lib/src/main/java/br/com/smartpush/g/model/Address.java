package br.com.smartpush.g.model;

import android.util.Log;

import br.com.smartpush.u.SmartpushUtils;

/**
 * Created by fabio.licks on 09/02/16.
 */
public class Address {

    public String countryCode;
    public String country;
    public String state;
    public String city;
    public String neighborhood;

    public Address() { }

    public Address( String countryCode, String country, String state, String city, String neighborhood ) {
        this.countryCode = countryCode;
        this.country = country;
        this.state = state;
        this.city = city;
        this.neighborhood = neighborhood;

        Log.d( SmartpushUtils.TAG, toString() );
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
