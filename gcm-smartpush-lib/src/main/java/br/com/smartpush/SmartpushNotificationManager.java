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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static br.com.smartpush.SmartpushService.ACTION_NOTIF_CANCEL;
import static br.com.smartpush.SmartpushService.ACTION_NOTIF_REDIRECT;
import static br.com.smartpush.SmartpushService.ACTION_NOTIF_UPDATABLE;
import static br.com.smartpush.Utils.CommonUtils.getValue;
import static br.com.smartpush.Utils.Constants.LAUNCH_ICON;
import static br.com.smartpush.Utils.Constants.NOTIF_AUTO_CANCEL;
import static br.com.smartpush.Utils.Constants.NOTIF_BANNER;
import static br.com.smartpush.Utils.Constants.NOTIF_CATEGORY;
import static br.com.smartpush.Utils.Constants.NOTIF_CATEGORY_BUSCAPE;
import static br.com.smartpush.Utils.Constants.NOTIF_DETAIL;
import static br.com.smartpush.Utils.Constants.NOTIF_TITLE;
import static br.com.smartpush.Utils.Constants.NOTIF_VIBRATE;
import static br.com.smartpush.Utils.Constants.NOTIF_VIDEO_URI;
import static br.com.smartpush.Utils.Constants.PUSH_DEFAULT_ICONS;
import static br.com.smartpush.Utils.Constants.PUSH_INTERNAL_ID;
import static br.com.smartpush.Utils.TAG;

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
        "open_url_in_browser":1,
        "url": "",
        "package": "",
        "ac": "",
        "vib": "",
        "video": "",
        "play_video_only_on_wifi": 1,
        "color":""
    },
    "extras": {
        "animate": true,

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
    ADDED:
    Atributo "color" foi adicionado a notificacao.

    REMOVED:
    Atributo "play_video_only_on_wifi" foi removido.
    Atributo "video" foi removido dos frames!
    Atributo "animateRate"  foi removido dos frames!
    Atributo "package" foi removido dos frames!
 */

public class SmartpushNotificationManager {

    private Context mContext;

    public SmartpushNotificationManager( Context context ) {
        mContext = context;
    }

    public void onMessageReceived( String from, Bundle data ) {
        // TODO revisar
        SmartpushLog.d( Utils.TAG, "PACKAGE_NAME: " + mContext.getApplicationContext().getPackageName() );

        if ( data != null && !data.isEmpty() ) {  // has effect of unparcelling Bundle
            String pushId =
                    SmartpushHitUtils.getValueFromPayload(
                            SmartpushHitUtils.Fields.PUSH_ID, data );

            // MUTABLE NOTIFICATION SCHEDULE - begin
            // TODO: MUTABLE NOTIFICATION : implementar, revisar e liberar em uma versao futura da SDK.
            // scheduleNotificationRefreshTime( data );
            // MUTABLE NOTIFICATION SCHEDULE - end

            // Retrieve updated payload
            data = SmartpushHttpClient.getPushPayload( mContext, pushId, data );

            // If has "video" attribute in bundle prefetch
            if ( data.containsKey( NOTIF_VIDEO_URI ) ) {
                // Prefetching video...
                String midiaId =
                        data.getString( NOTIF_VIDEO_URI, null );

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
                .setSmallIcon( getPushIcon( extras ) )               // Set Small Icon
                .setAutoCancel   ( isAutoCancel( extras ) )          // Set Auto Cancel Action
                .setContentIntent( addMainAction( extras ) )         // Set Main Action
                .setDeleteIntent ( addDeleteAction( extras ) )       // Set Delete Action
                .setContentTitle ( extras.getString( NOTIF_TITLE ) ) // Set Title
                .setContentText  ( extras.getString( NOTIF_DETAIL ) )// Set 2nd line
                .setWhen( System.currentTimeMillis() )               // Set WHEN ARRIVE
                .setLights( Color.GREEN, 1000, 5000 )                // Set LIGHT Color and pattern
                .setPriority( 5 );    // NotificationCompat.PRIORITY_HIGH

        if ( vibrate( extras ) ) {                                   // NOTIF_VIBRATE
            builder.setVibrate(  new long[] { 100, 500, 200, 800 } );
        }

        if ( extras.containsKey( "color" ) ) {
            int color = Color.parseColor( extras.getString( "color" ) );
            builder.setColor( color );
        }

        setBigIcon( extras, builder );                               // Set Large Icon

        addSecondaryActions( extras, builder );                      // Set Secondary Actions

        String pushType  =
                ( extras.containsKey( "type" ) )
                        ? extras.getString( "type" )
                        : ( extras.containsKey( "adtype" ) ? extras.getString( "adtype" ) : "PUSH" );

        SmartpushLog.d( Utils.TAG, "pushType: " + pushType );

        int pushTypeOrder =
                Arrays.asList(
                        new String[] {
                                "PUSH", "PUSH_AD", "PUSH_BANNER_AD",
                                "BANNER", "SLIDER", "CARROUSSEL",
                                "SUBSCRIBE_EMAIL", "SUBSCRIBE_PHONE",
                                "CARROUSSEL_BUSCAPE", "SLIDER_BUSCAPE"} )
                        .indexOf( pushType );

        SmartpushLog.d( Utils.TAG, "pushTypeOrder: " + pushTypeOrder );

        // https://medium.com/@britt.barak/notifications-part-3-going-custom-31c31609f314
        // https://medium.com/@britt.barak/notifications-part1-styling-930ec3d7caa5

        switch ( pushTypeOrder ) {
            case 0:
            case 1:
            case 2:
                // PUSH, PUSH_AD, PUSH_BANNER_AD
                if ( extras.containsKey( NOTIF_BANNER ) ) {
                    if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
                        NotificationCompat.BigPictureStyle style = createBigPictureStyle( extras );
                        if ( style != null ) {
                            builder.setStyle( style );                      // Set Big Banner
                        }
                    }
                }
                break;
            case 3:
                // BANNER
                // TODO: RICH PUSH (BANNER) : implementar e revisar para proxima versao da SDK
//                RemoteViews remote = setSmartpushBannerNotification( extras );
//                if ( remote != null ) {
//                    builder.setCustomContentView( remote );
//                }
                break;
            case 4:
            case 5:
                // SLIDER, CARROUSSEL
                // TODO: RICH PUSH (SLIDER, CARROUSSEL) : implementar e revisar para proxima versao da SDK
//                remote = setSmartpushRichNotification( extras );
//                if ( remote != null ) {
//                    builder.setCustomBigContentView(remote);
//                }
                break;
            case 6:
            case 7:
                // SUBSCRIBE_EMAIL, SUBSCRIBE_PHONE
                // TODO: RICH PUSH (SUBSCRIBE_EMAIL, SUBSCRIBE_PHONE) : implementar e revisar para proxima versao da SDK
                // builder.setCustomContentView( )        // small
                // builder.setCustomBigContentView( )    // big
                break;
            case 8:
            case 9:
                // CUSTOM_BUSCAPE
                RemoteViews remote = setSmartpushRichNotification( extras );
                if ( remote != null ) {
                    builder.setCustomBigContentView( remote );
                }
                break;
            default:
                SmartpushLog.d( Utils.TAG, "Push Type unknow" );
        }

        NotificationManagerCompat nm = NotificationManagerCompat.from( mContext );
//        nm.cancel( PUSH_INTERNAL_ID );
        nm.notify( PUSH_INTERNAL_ID, builder.build() );
    }

//    private RemoteViews setSmartpushBannerNotification( Bundle data ) {
//
//        return null;
//    }

    private RemoteViews setSmartpushRichNotification( Bundle data ) {
        // RemoteView
        RemoteViews remoteViews = null;

        String extras =
                ( data.containsKey( Utils.Constants.PUSH_EXTRAS )
                        ? data.getString( Utils.Constants.PUSH_EXTRAS ) : null );

        if ( extras != null ) {
            JSONObject payloadExtra = null;
            try {
                payloadExtra = new JSONObject( extras );
                SmartpushLog.d( Utils.TAG, " ------------> \n " + payloadExtra.toString( 4 ) );
            } catch ( JSONException e ) {
                SmartpushLog.e( Utils.TAG, e.getMessage(), e );
                return null;
            }

            boolean animate = false;
//                        ( payloadExtra.has( "animate" ) ) ? payloadExtra.getBoolean( "animate" ) : false;

            if ( animate ) {
                // TODO adjust animate true!
                throw new RuntimeException( "Not implemented yet" );
            } else {
                SmartpushLog.d(Utils.TAG, " ------------> FETCHING BANNERS! ");
                List<SlideInfo> slides = new ArrayList<>();

                for (int i = 0; i < 5; i++) {
                    if (payloadExtra.has("frame:" + (i + 1) + ":banner")
                            && (payloadExtra.has("frame:" + (i + 1) + ":url"))) {

                        try {
                            SlideInfo slide = new SlideInfo();
                            slide.bitmap =
                                    CacheManager
                                            .getInstance(mContext)
                                            .loadBitmap(payloadExtra.getString("frame:"
                                                    + (i + 1) + ":banner"), CacheManager.ExpirationTime.DAY);

                            slide.url = payloadExtra.getString("frame:" + (i + 1) + ":url");

                            if (slide.bitmap != null && slide.url != null) {
                                slides.add(slide);
                                SmartpushLog.d(Utils.TAG, slide.toString());
                            }
                        } catch ( JSONException e ) {
                            SmartpushLog.e(Utils.TAG, e.getMessage(), e);
                        }
                    }
                }

                switch ( slides.size() ) {
                    case 0:
                        return null;
                    case 1:
                        remoteViews =
                                new RemoteViews(
                                        mContext.getPackageName(), R.layout.notif_icon_text_carroussel_buscape_1);
                        break;
                    case 2:
                        remoteViews =
                                new RemoteViews(
                                        mContext.getPackageName(), R.layout.notif_icon_text_carroussel_buscape_2);
                        break;
                    case 3:
                        remoteViews =
                                new RemoteViews(
                                        mContext.getPackageName(), R.layout.notif_icon_text_carroussel_buscape_3);
                        break;
                    case 4:
                        remoteViews =
                                new RemoteViews(
                                        mContext.getPackageName(), R.layout.notif_icon_text_carroussel_buscape_4);
                        break;
                    case 5:
                        remoteViews =
                                new RemoteViews(
                                        mContext.getPackageName(), R.layout.notif_icon_text_carroussel_buscape_5);
                        break;
                    default:
                        return null;
                }

                // Set Title
                if (data.containsKey(NOTIF_TITLE)) {
                    remoteViews
                            .setTextViewText(R.id.title, data.getString(NOTIF_TITLE));
                }

                // Set 2nd line
                if (data.containsKey(NOTIF_DETAIL)) {
                    remoteViews
                            .setTextViewText(R.id.subtitle, data.getString(NOTIF_DETAIL));
                }

                if ( slides.size() > 1 ) {
                    SmartpushLog.d(Utils.TAG, " ------------> CONFIG NAV BUTTONS! START ");
                    // config navigate buttons
                    Intent itNext = new Intent(mContext, SmartpushService.class)
                            .setAction(ACTION_NOTIF_UPDATABLE)
                            .putExtras(data)
                            .putExtra("flip.next", true)
                            .putExtra("flip.previous", false);

                    remoteViews
                            .setOnClickPendingIntent(
                                    R.id.btnNext, PendingIntent.getService(mContext, 0, itNext, 0));

                    Intent itPrevious = new Intent(mContext, SmartpushService.class)
                            .setAction(ACTION_NOTIF_UPDATABLE)
                            .putExtras(data)
                            .putExtra("flip.next", false)
                            .putExtra("flip.previous", true);

                    remoteViews
                            .setOnClickPendingIntent(
                                    R.id.btnPrevious, PendingIntent.getService(mContext, 0, itPrevious, 0));

                    SmartpushLog.d(Utils.TAG, " ------------> CONFIG NAV BUTTONS! END ");

                    // Adjust viewflipper visibility & move cards
                    if (data.getBoolean("flip.next", false)) {
                        SmartpushLog.d(Utils.TAG, " ------------> GOING TO NEXT! ");
                        remoteViews.setViewVisibility(R.id.carroussel_next, View.VISIBLE);
                        remoteViews.setViewVisibility(R.id.carroussel_previous, View.GONE);
                        remoteViews.showNext(R.id.carroussel_next);
                        remoteViews.showNext(R.id.carroussel_previous);
                    } else if (data.getBoolean("flip.next", true)) {
                        SmartpushLog.d(Utils.TAG, " ------------> GOING TO PREV! ");
                        remoteViews.setViewVisibility(R.id.carroussel_previous, View.VISIBLE);
                        remoteViews.setViewVisibility(R.id.carroussel_next, View.GONE);
                        remoteViews.showPrevious(R.id.carroussel_previous);
                        remoteViews.showPrevious(R.id.carroussel_next);
                    }

                    SmartpushLog.d(Utils.TAG, " ------------> SETTING SLIDES! ");
                }

                // set cards
                int[] ids_previous = {
                        R.id.frame_1_previous,
                        R.id.frame_2_previous,
                        R.id.frame_3_previous,
                        R.id.frame_4_previous,
                        R.id.frame_5_previous
                };

                int[] ids_next = {
                        R.id.frame_1_next,
                        R.id.frame_2_next,
                        R.id.frame_3_next,
                        R.id.frame_4_next,
                        R.id.frame_5_next
                };

                for ( int i = 0; slides != null && i < slides.size(); i++ ) {
                    SlideInfo slide = slides.get( i );

                    Intent actionIntent =
                            Utils.Smartpush
                                    .getIntentToRedirect(mContext, slide.url, null, data);

                    remoteViews
                            .setOnClickPendingIntent(
                                    R.id.root, PendingIntent.getService(mContext, 0, actionIntent, 0));

                    if ( slides.size() == 1 ) {
                        remoteViews.setImageViewBitmap( R.id.frame_1, slide.bitmap);
                    } else {
                        remoteViews.setImageViewBitmap(ids_previous[i], slide.bitmap);
                        remoteViews.setImageViewBitmap(ids_next[i], slide.bitmap);
                    }
                }
            }
        }

        SmartpushLog.d( Utils.TAG, " ------------> DONE! " );
        return remoteViews;
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
                builder.setLargeIcon(
                        BitmapFactory.decodeResource( resources, R.drawable.ic_sp_buscape ) );
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
            return Utils.DeviceUtils.hasPermissions( mContext, Manifest.permission.VIBRATE );
        }

        return false;
    }

    public PendingIntent addMainAction( Bundle extras ) {
        Intent serviceIntent = new Intent( mContext, SmartpushService.class);
        serviceIntent.setAction( ACTION_NOTIF_REDIRECT );
        serviceIntent.putExtras( extras );

        PendingIntent servicePendingIntent =
                PendingIntent.getService( mContext,
                        // integer constant used to identify the service
                        SmartpushService.SERVICE_ID,
                        serviceIntent,
                        // FLAG to avoid creating a second service if there's already one running
                        PendingIntent.FLAG_CANCEL_CURRENT );

        // Creates and return the PendingIntent
        return servicePendingIntent;
    }

    public PendingIntent addDeleteAction( Bundle extras ) {
        Intent serviceIntent = new Intent( mContext, SmartpushService.class);
        serviceIntent.setAction( ACTION_NOTIF_CANCEL );
        serviceIntent.putExtras( extras );

        PendingIntent servicePendingIntent =
                PendingIntent.getService( mContext,
                        // integer constant used to identify the service
                        SmartpushService.SERVICE_ID,
                        serviceIntent,
                        // FLAG to avoid creating a second service if there's already one running
                        PendingIntent.FLAG_CANCEL_CURRENT );

        // Creates and return the PendingIntent
        return servicePendingIntent;
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
            int newWidth    = 478; //metrics.widthPixels;

            float scaleFactor = ( float ) newWidth / ( float ) imageWidth;

            int newHeight = ( int )( imageHeight * scaleFactor );

            SmartpushLog.d( TAG, "Picture size: [" + newWidth + "," + newHeight + "]" );

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

    public void scheduleNotificationRefreshTime( Bundle data ) {
        // Get update iteration
        int pushUpdateCount = data.getInt( Utils.Constants.PUSH_UPDATE_COUNT, 0 );
        pushUpdateCount++;
        data.putInt( Utils.Constants.PUSH_UPDATE_COUNT, pushUpdateCount );

        if ( pushUpdateCount == 10 ) {
            SmartpushLog.d( TAG,
                    "-------------------> CANCELLING REFRESH AFTER [" + pushUpdateCount + "] ITERATIONS." );
            return;
        } else {
            SmartpushLog.d( TAG,
                    "-------------------> SETTING REFRESH TIME [" + pushUpdateCount + "]" );
        }

        Intent serviceIntent =
                new Intent( mContext, SmartpushService.class)
                        .setAction( ACTION_NOTIF_UPDATABLE );

        serviceIntent.putExtras( data );

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