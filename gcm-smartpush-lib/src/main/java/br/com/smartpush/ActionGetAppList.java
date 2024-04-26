package br.com.smartpush;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static br.com.smartpush.Utils.TAG;

class ActionGetAppList {

    public static final String ACTION_GET_APP_LIST = "action.GET_APP_LIST";

    /**
     * Starts this service to perform action retrieve a list of apps with no parameters. If
     * the service is already performing a task this action will be queued.
     */
    public static void getAppList( Context context ) {
        Intent intent = new Intent( context, SmartpushService.class ) ;
        intent.setAction( ACTION_GET_APP_LIST );
        SmartpushService.start(intent, context);
    }

    // TODO implementar LIST APPS na forma de broadcast!
    public static void handleActionSaveAppsListState( Context context ) {
        SQLiteDatabase db = new DatabaseManager( context ).getWritableDatabase();

        // Active apps list
        List<String> installedAppsList = getInstalledApps( context );

        // List with last state sinc to SMARTPUSH
        List<AppInfo> savedList = AppInfoDAO.listAll( db );

        Log.d( TAG, "savedList: " + savedList.size() );
        Log.d( TAG, "installedAppsList: " + installedAppsList.size() );

        // insert/update packages installed state
        for ( String packageName : installedAppsList ) {
            boolean found = false;
            for( AppInfo saved : savedList ) {
                if ( packageName.equals( saved.packageName ) ) {
                    Log.d( TAG, "savedList.contains: " + packageName );
                    if ( saved != null && saved.state == AppInfo.UNINSTALLED ) {
                        saved.state = AppInfo.INSTALLED;
                        saved.sinc = false;
                        AppInfoDAO.save( db, saved );
                    }

                    found = true;
                    saved.match = found;
                    break;
                }
            }

            if ( !found ) {
                Log.d( TAG, "savedList.not.contains: " + packageName );
                AppInfo newApp = new AppInfo();
                newApp.packageName = packageName;
                newApp.state = AppInfo.INSTALLED;
                newApp.sinc = false;
                newApp.match = true;

                AppInfoDAO.save( db, newApp );
            }
        }

        // mark packages were uninstalled
        for ( AppInfo item : savedList ) {
            if ( !item.match ) {
                Log.d( TAG, "savedList.contains.uninstalled.app: " + item.packageName );
                item.state = AppInfo.UNINSTALLED;
                item.sinc = false;

                AppInfoDAO.save( db, item );
            }
        }

        // renew list with last state
        savedList = AppInfoDAO.listAll( db );

        List<String> uninstalled = new ArrayList<>();
        List<String> installed   = new ArrayList<>();

        for ( AppInfo item : savedList ) {
            if ( !item.sinc ) {
                if ( item.state == AppInfo.INSTALLED ) {
                    Log.d( TAG, "INSTALLED: " + item.toString() );
                    installed.add( item.packageName );
                }

                if ( item.state == AppInfo.UNINSTALLED ) {
                    Log.d( TAG, "UNINSTALLED: " + item.toString() );
                    uninstalled.add( item.packageName );
                }

                // LIST APPS - revisar para proxima versao da SDK, completar a operacao de persistencia
//                item.setSinc( SmartpushConnectivityUtil.isConnected( this ) );
//                AppInfoDAO.save( db, item );
            }
        }

        // Release
        db.close();
    }

    private static ArrayList<String> getInstalledApps( Context _c ) {
        PackageManager packageManager = _c.getPackageManager();
        List<ApplicationInfo> list =
                checkForLaunchIntent( packageManager,
                        packageManager.getInstalledApplications( PackageManager.GET_META_DATA ) );

        ArrayList<String> out = new ArrayList<>();
        for ( ApplicationInfo info : list ) {
            if ( ( info.flags & ApplicationInfo.FLAG_SYSTEM ) == 1 ) {
                // System application just ignore!
                continue;
            } else {
                // Application installed by user
                //			info.loadLabel(packageManager)
                //			info.packageName;
                //			info.loadIcon(packageManager)

                out.add( info.packageName );
            }
        }

        // return ( new JSONArray( out ) ).toString();
        return out;
    }

    private static List<ApplicationInfo> checkForLaunchIntent( PackageManager packageManager, List<ApplicationInfo> list ) {
        ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
        for ( ApplicationInfo info : list ) {
            try {
                if ( null != packageManager.getLaunchIntentForPackage( info.packageName ) ) {
                    applist.add( info );
                }
            } catch ( Exception e ) {
                SmartpushLog.e( TAG, e.getMessage(), e );
            }
        }

        return applist;
    }
}
