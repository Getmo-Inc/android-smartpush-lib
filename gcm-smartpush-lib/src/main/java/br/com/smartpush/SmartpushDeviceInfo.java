package br.com.smartpush;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public final class SmartpushDeviceInfo implements Parcelable {

    public String alias;
	public String regId;
    public String hwId;
	public String optout;
	public String createdAt;
	
	public SmartpushDeviceInfo( String token ) {
        regId = token;
    }

	public SmartpushDeviceInfo( Parcel in ) {
		alias  = in.readString();
		regId  = in.readString();
        hwId   = in.readString();
		optout = in.readString();
		createdAt = in.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags ) {
        dest.writeString( alias );
        dest.writeString( regId );
        dest.writeString( hwId );
        dest.writeString( optout );
        dest.writeString( createdAt );
	}

    @Override
    public String toString() {
        return "{ \"alias\":\"" + alias +
                "\", \"regId\":\"" + regId +
                "\", \"hwId\":\"" + hwId +
                "\", \"optout\":\"" + optout +
                "\", \"createdAt\":\"" + createdAt + "\"}";
    }

    public static final Parcelable.Creator<SmartpushDeviceInfo> CREATOR = new Parcelable.Creator<SmartpushDeviceInfo>() {
		@Override
		public SmartpushDeviceInfo createFromParcel(Parcel source) {
			return new SmartpushDeviceInfo(source);
		}

		@Override
		public SmartpushDeviceInfo[] newArray(int size) {
			return new SmartpushDeviceInfo[size];
		}
	};

	public static SmartpushDeviceInfo bind( Context context, JSONObject json ) throws JSONException {
		SmartpushDeviceInfo deviceInfo =
				new SmartpushDeviceInfo(
						Utils.PreferenceUtils.readFromPreferences(
								context, Utils.Constants.SMARTP_REGID ) );

		if ( json.has( "alias" ) ) {
			deviceInfo.alias  = json.getString( "alias" );
		}

		if ( json.has( "regid" ) ) {
			deviceInfo.regId  = json.getString( "regid" );
		}

		if ( json.has( "optout" ) ) {
			deviceInfo.optout = json.getString( "optout" );
		}

		if ( json.has( "created_at" ) ) {
			JSONObject creation = json.getJSONObject( "created_at" );
			if ( creation != null && creation.has( "date" ) ) {
				deviceInfo.createdAt =
						creation.getString("date" );
			}
		}

		return deviceInfo;
	}
}