package br.com.smartpush;

/**
 * Created by t.licks on 23/08/17.
 */

class AppInfo {
    public static final int INSTALLED   = 1;
    public static final int UNINSTALLED = 0;

    public static final String ID = "ID";
    public static final String PACKAGE_NAME = "APP_PACKAGE_NAME";
    public static final String SINC_STATE = "SINC_STATE";
    public static final String STATE = "APP_STATE";

    private int _id;
    private String packageName;
    private boolean sinc;
    private int state;

    public int getId() {
        return _id;
    }

    public void setId( int id ) {
        this._id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName( String packageName ) {
        this.packageName = packageName;
    }

    public boolean isSinc() {
        return sinc;
    }

    public void setSinc( boolean sinc ) {
        this.sinc = sinc;
    }

    public int getState() {
        return state;
    }

    public void setState( int state ) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "{ \"packagename\":\"" + packageName + "\"" +
                ", \"state\":\"" + ( ( state == INSTALLED ) ? "INSTALLED" : "UNINSTALLED" ) + "\"" +
                ", \"sinc\":" + sinc + "" + "}";
    }
}
