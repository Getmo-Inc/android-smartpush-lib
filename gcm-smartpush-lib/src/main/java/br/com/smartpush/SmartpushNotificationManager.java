package br.com.smartpush;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.Arrays;
import java.util.Calendar;

import static android.content.ContentValues.TAG;
import static br.com.smartpush.SmartpushService.ACTION_NOTIF_UPDATABLE;
import static br.com.smartpush.Utils.CommonUtils.getValue;
import static br.com.smartpush.Utils.Constants.LAUNCH_ICON;
import static br.com.smartpush.Utils.Constants.NOTIF_AUTO_CANCEL;
import static br.com.smartpush.Utils.Constants.NOTIF_BANNER;
import static br.com.smartpush.Utils.Constants.NOTIF_CATEGORY;
import static br.com.smartpush.Utils.Constants.NOTIF_CATEGORY_BUSCAPE;
import static br.com.smartpush.Utils.Constants.NOTIF_DETAIL;
import static br.com.smartpush.Utils.Constants.NOTIF_TITLE;
import static br.com.smartpush.Utils.Constants.NOTIF_URL;
import static br.com.smartpush.Utils.Constants.NOTIF_VIBRATE;
import static br.com.smartpush.Utils.Constants.NOTIF_VIDEO_URI;
import static br.com.smartpush.Utils.Constants.ONLY_PORTRAIT;
import static br.com.smartpush.Utils.Constants.PUSH_DEFAULT_ICONS;
import static br.com.smartpush.Utils.Constants.PUSH_INTERNAL_ID;

/**
 * Created by t.licks on 28/07/17.
 */

/**
 * PAYLOAD
 *
 * {
    "params": {
        "type": "BANNER|SLIDER|CARROUSSEL|SUBSCRIBE_EMAIL|SUBSCRIBE_PHONE",
        "provider": "SMARTPUSH",
        "icon": "",
        "category": 1,
        "title": "",
        "detail": "",
        "banner": "",
        "url": "",
        "package": "",
        "ac": "",
        "vib": "",
        "video": "",
        "play_video_only_on_wifi": 1
    },
    "extras": {
        "animate": true,
        "animateRate": 3000,

        "frame:1:banner": "",
        "frame:1:url": "",
        "frame:1:package": "",

        "frame:2:banner": "",
        "frame:2:url": "",
        "frame:2:package": "",

        "frame:3:banner": "",
        "frame:3:url": "",
        "frame:3:package": "",

        "frame:4:banner": "",
        "frame:4:url": "",
        "frame:4:package": "",

        "frame:5:banner": "",
        "frame:5:url": "",
        "frame:5:package": ""
    }
 }

 Changes:
     Atributo "video" foi removido dos frames!
 */

public class SmartpushNotificationManager {

    private Context mContext;

    public SmartpushNotificationManager( Context context ) {
        mContext = context;
    }

    public void onMessageReceived( String from, Bundle data ) {
        if ( data != null && !data.isEmpty() ) {  // has effect of unparcelling Bundle
            String pushId =
                    SmartpushHitUtils.getValueFromPayload(
                            SmartpushHitUtils.Fields.PUSH_ID, data );

            // Retrieve updated payload
            data = SmartpushHttpClient.getPushPayload( mContext, pushId, data );

            // If has "video" attribute in bundle prefetch
            if ( data.containsKey( NOTIF_VIDEO_URI ) ) {
                // Prefetching video...
                String midiaId =
                        data.getString(NOTIF_VIDEO_URI, null );

                CacheManager
                        .getInstance( mContext )
                        .prefetchVideo( midiaId, CacheManager.ExpirationTime.NONE );
            }

            createNotification( data );
        }
    }

    // Build Notification
    private void createNotification( Bundle extras ) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder( mContext )
                .setSmallIcon(getPushIcon(extras))                   // Set Small Icon
                .setAutoCancel( isAutoCancel( extras ) )             // Set Auto Cancel Action
                .setContentIntent( addMainAction( extras ) )         // Set Main Action
                .setContentTitle ( extras.getString(NOTIF_TITLE) )   // Set Title
                .setContentText  ( extras.getString(NOTIF_DETAIL) )  // Set 2nd line
                .setWhen( System.currentTimeMillis() )               // Set WHEN ARRIVE
                .setLights( Color.GREEN, 1000, 5000 )                // Set LIGHT Color and pattern
                .setPriority( NotificationCompat.PRIORITY_HIGH );

        if ( vibrate( extras ) ) {                                   // NOTIF_VIBRATE
            builder.setVibrate(  new long[] { 100, 500, 200, 800 } );
        }

        addSecondaryActions( extras, builder );                      // Set Secondary Actions
        setBigIcon( extras, builder );                               // Set Large Icon

        String pushType  =
                ( extras.containsKey( "type" ) )
                        ? extras.getString( "type" )
                        : extras.getString( "adtype" );

        if ( pushType != null && !"".equals( pushType.trim() ) ) {
            int pushTypeOrder =
                    Arrays.asList(
                            new String[] { "BANNER", "SLIDER", "CARROUSSEL", "SUBSCRIBE_EMAIL", "SUBSCRIBE_PHONE" } )
                            .indexOf( pushType );

            // TODO working here ..


        } else {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
                NotificationCompat.BigPictureStyle style = createBigPictureStyle( extras );
                if ( style != null ) {
                    builder.setStyle( style );                      // Set Big Banner
                }
            }
        }


        NotificationManagerCompat nm = NotificationManagerCompat.from( mContext );
//        nm.cancel( PUSH_INTERNAL_ID );
        nm.notify( PUSH_INTERNAL_ID, builder.build() );
    }

    private int getPushIcon( Bundle extras ) {
        int category =
                Integer.parseInt( getValue( extras.getString( NOTIF_CATEGORY ), "1" )  ) ;

        category = ( category >= PUSH_DEFAULT_ICONS.length ) ? 1 : category;

        return PUSH_DEFAULT_ICONS[ category - 1 ];
    }

    private void setBigIcon( Bundle extras, NotificationCompat.Builder builder ) {
        String urlpath = extras.getString( LAUNCH_ICON );

        Resources resources = mContext.getResources();

        if ( urlpath == null ) {
            int category =
                    Integer.parseInt( getValue( extras.getString( NOTIF_CATEGORY ), "1" ) ) ;

            category = ( category >= PUSH_DEFAULT_ICONS.length ) ? 1 : category;

            if ( category == NOTIF_CATEGORY_BUSCAPE ) {
                builder.setLargeIcon( BitmapFactory.decodeResource( resources, R.drawable.ic_sp_buscape ) );
            }

            return;
        }

//			int h = ( int ) getResources().getDimension( android.R.dimen.notification_large_icon_height );
//			int w = ( int ) getResources().getDimension( android.R.dimen.notification_large_icon_width );

        float scaleFactor = resources.getDisplayMetrics().density;
        int size = ( int ) ( 48 * scaleFactor + 0.5f );

        Bitmap b =
                CacheManager
                        .getInstance( mContext )
                        .loadBitmap( urlpath, CacheManager.ExpirationTime.DAY );

        if ( b != null ) {
            builder.setLargeIcon( Bitmap.createScaledBitmap( b, size, size, false ) );
        }
    }

    private boolean isAutoCancel( Bundle extras ) {
        return ( "0".equals( extras.getString( NOTIF_AUTO_CANCEL ) ) ) ? false : true;
    }

    private boolean vibrate( Bundle extras ) {
        if ( ( "1".equals( extras.getString( NOTIF_VIBRATE ) ) ) ? true : false ) {
//            int permissionCheck =
//                    ContextCompat.checkSelfPermission( mContext, Manifest.permission.VIBRATE );
//            return ( permissionCheck == PackageManager.PERMISSION_GRANTED );

            return Utils.DeviceUtils.hasPermissions( mContext, Manifest.permission.VIBRATE );
        }

        return false;
    }

    private PendingIntent addMainAction( Bundle extras ) {
        String action = extras.getString( NOTIF_URL );

        // No main action defined!
        if ( action == null ) {
            return null;
        }

        Intent it;

        if ( action.startsWith( "market://details?id=" ) ) {
            it = new Intent( Intent.ACTION_VIEW );
            it.setData( Uri.parse( action ) );
        } else {
            it = new Intent( mContext, SmartpushActivity.class );
            if ( !extras.containsKey(NOTIF_VIDEO_URI) ) {
                extras.putBoolean( ONLY_PORTRAIT, true );  // Lock screen orientation
            }

            it.putExtras( extras ).addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
        }

        // Creates and return the PendingIntent
        return PendingIntent.getActivity( mContext, 0, it, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    private void addSecondaryActions( Bundle extras, NotificationCompat.Builder builder ) {

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
            int count = 0;

            String act = extras.getString( "CALL" );
            if ( act != null ) {
                builder.addAction( R.drawable.ic_call, mContext.getString( R.string.call ),
                        PendingIntent.getActivity( mContext, 0, SmartpushIntentUtils.dialPhone( act ), 0 ) );
                count++;
            }

            act = extras.getString( "SMS" );
            if ( act != null ) {
                String[] params = act.split( ";" );
                if ( params.length == 2 ) {
                    builder.addAction( R.drawable.ic_sms, mContext.getString( R.string.sms ),
                            PendingIntent
                                    .getActivity(
                                            mContext, 0, SmartpushIntentUtils.sendSms(
                                                    mContext, params[0], params[1]), 0) );
                    count++;
                }
            }

            act = extras.getString( "EMAIL" );
            if ( act != null ) {
                String[] params = act.split( ";" );
                if ( params.length == 3 ) {
                    builder.addAction( R.drawable.ic_email, mContext.getString( R.string.email ),
                            PendingIntent
                                    .getActivity(
                                            mContext, 0, SmartpushIntentUtils.sendEmail(
                                                    params[0], params[1], params[2]), 0) );
                    count++;
                }
            }

            act = extras.getString( "MAPS" );
            if ( act != null && count < 3 ) {
                String[] params = act.split( ";" );
                if ( params.length == 2 ) {
                    builder.addAction( R.drawable.ic_mapas, mContext.getString( R.string.map ),
                            PendingIntent
                                    .getActivity(
                                            mContext, 0, SmartpushIntentUtils.showLocation(
                                                    Float.parseFloat(params[0]),
                                                    Float.parseFloat(params[1]), 13), 0) );
                    count++;
                }
            }

            act = extras.getString( "SHARE" );
            if ( act != null && count < 3 ) {
                String[] params = act.split( ";" );
                if ( params.length == 2 ) {
                    builder.addAction( R.drawable.ic_compartilhar, mContext.getString( R.string.share ),
                            PendingIntent.getActivity( mContext, 0,
                                    SmartpushIntentUtils.shareText( params[ 0 ], params[ 1 ] ), 0 ) );
                }
            }
        }
    }

    private NotificationCompat.BigPictureStyle createBigPictureStyle( Bundle extras ) {
        Bitmap bitmap =
                CacheManager
                        .getInstance( mContext )
                        .loadBitmap( extras.getString( NOTIF_BANNER ), CacheManager.ExpirationTime.DAY );

        if ( bitmap != null ) {
            // Resize picture
            WindowManager wm = ( WindowManager) mContext.getSystemService( Context.WINDOW_SERVICE );
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics( metrics );

            int imageWidth  = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();
            int newWidth    = 400; //metrics.widthPixels;

            float scaleFactor = ( float ) newWidth / ( float ) imageWidth;

            int newHeight = ( int )( imageHeight * scaleFactor );

            SmartpushLog.d( TAG, "Picture size: " + newWidth + "," + newHeight );

            Bitmap resizedBitmap = Bitmap.createScaledBitmap( bitmap, newWidth, newHeight, true );

            // Big Picture Style config
            NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle()
                    .setBigContentTitle(extras.getString( NOTIF_TITLE ) )
                    .setSummaryText( extras.getString( NOTIF_DETAIL ) )
                    .bigPicture( resizedBitmap );

            return notiStyle;
        }

        return null;
    }

    public void scheduleNotificationRefreshTime() {
        SmartpushLog.d( TAG, "-------------------> SETTING REFRESH TIME" );
        Intent serviceIntent =
                new Intent( mContext, SmartpushService.class)
                        .setAction(ACTION_NOTIF_UPDATABLE);

        // make sure you **don't** use *PendingIntent.getBroadcast*, it wouldn't work
        PendingIntent servicePendingIntent =
                PendingIntent.getService( mContext,
                        // integer constant used to identify the service
                        SmartpushService.SERVICE_ID,
                        serviceIntent,
                        // FLAG to avoid creating a second service if there's already one running
                        PendingIntent.FLAG_CANCEL_CURRENT );

        Calendar cal = Calendar.getInstance();
        cal.add( Calendar.MINUTE, 1 );

        /** this gives us the time for the first trigger.  */
        AlarmManager am =
                ( AlarmManager ) mContext.getSystemService( Context.ALARM_SERVICE );

        // there are other options like setInexactRepeating, check the docs
        am.set( AlarmManager.RTC_WAKEUP, //type of alarm. This one will wake up the device when it goes off, but there are others, check the docs
                cal.getTimeInMillis(),
                servicePendingIntent );
    }
}