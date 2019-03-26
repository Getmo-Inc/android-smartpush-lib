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

    public int _id;
    public String packageName;
    public boolean sinc;
    public int state;

    // Control evaluating
    public boolean match;

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        AppInfo appInfo = ( AppInfo ) o;

        return packageName.equals( appInfo.packageName );
    }

    @Override
    public int hashCode() {
        return packageName.hashCode();
    }

    @Override
    public String toString() {
        return "{ \"packagename\":\"" + packageName + "\"" +
                ", \"state\":\"" + ( ( state == INSTALLED ) ? "INSTALLED" : "UNINSTALLED" ) + "\"" +
                ", \"sinc\":" + sinc + "" + "}";
    }
}
