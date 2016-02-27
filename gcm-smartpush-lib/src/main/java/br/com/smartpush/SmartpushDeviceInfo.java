package br.com.smartpush;

import android.os.Parcel;
import android.os.Parcelable;

public final class SmartpushDeviceInfo implements Parcelable {

    public static final String EXTRA_DEVICE_INFO = "br.com.smartpush.extra.EXTRA_DEVICE_INFO";

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
}