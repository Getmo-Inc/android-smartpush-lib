package br.com.smartpush;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import static android.content.ContentValues.TAG;
import static br.com.smartpush.Utils.CommonUtils.getValue;
import static br.com.smartpush.Utils.Constants.ONLY_PORTRAIT;

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
        "frame:1:video": "",
        "frame:1:package": "",
        "frame:2:banner": "",
        "frame:2:url": "",
        "frame:2:video": "",
        "frame:2:package": "",
        "frame:3:banner": "",
        "frame:3:url": "",
        "frame:3:video": "",
        "frame:3:package": "",
        "frame:4:banner": "",
        "frame:4:url": "",
        "frame:4:video": "",
        "frame:4:package": "",
        "frame:5:banner": "",
        "frame:5:url": "",
        "frame:5:video": "",
        "frame:5:package": ""
    }
 }
 */

class SmartpushNotificationManager {

    private Context mContext;

    // Push metadata
    public static String TITLE        = "title";
    public static String DETAIL       = "detail";
    public static String URL          = "url";
    public static String VIDEO_URI    = "video";
    public static String PLAY_VIDEO_ONLY_WIFI = "play_video_only_on_wifi";
    public static String AUTO_CANCEL  = "ac";
    public static String VIBRATE      = "vib";
    public static String PACKAGENAME  = "package";
    public static String CATEGORY     = "category";

    public static String NOTIF_BANNER = "banner";
    public static String LAUNCH_ICON  = "icon";

    // TODO
    // Se o array de icones for alterado tem de ajustar o indice desta variavel.
    private static final int CATEGORY_BUSCAPE = 18;

    private int[] pushIcons = {
            R.drawable.ic_esporte,
            R.drawable.ic_cultura,
            R.drawable.ic_turismo,
            R.drawable.ic_noticias,
            R.drawable.ic_imoveis,
            R.drawable.ic_veiculos,
            R.drawable.ic_refeicoes,
            R.drawable.ic_vestuario,         // vestuario
            R.drawable.stat_notify_weather,  // outros
            R.drawable.ic_celular_tablets,
            R.drawable.ic_eletro_info,
            R.drawable.ic_eventos,
            R.drawable.ic_empregos_negocios,
            R.drawable.ic_promocoes,
            R.drawable.ic_bebes_criancas,
            R.drawable.ic_casa_jardim,
            R.drawable.ic_animais,
            R.drawable.ic_sp_notif_buscape  // BUSCAPE
    };

    private static final int PUSH_INTERNAL_ID = 427738108;

    public SmartpushNotificationManager( Context context ) {
        mContext = context;
    }

    public void onMessageReceived( String from, Bundle data ) {
        if ( data != null && !data.isEmpty() ) {  // has effect of unparcelling Bundle

            String adType = data.getString( "adtype" );
            String adNet  = data.getString( "adnetwork" );

            // Tracking
            String pushId = SmartpushHitUtils
                    .getValueFromPayload(SmartpushHitUtils.Fields.PUSH_ID, data);

            Smartpush.hit( mContext, pushId, null, null, SmartpushHitUtils.Action.RECEIVED, null);

            if ( "smartpush".equals( adNet ) ) {
                if ( "ICON_AD".equals( adType ) ) {

                    int permissionCheck = ContextCompat.checkSelfPermission( mContext,
                            "com.android.launcher.permission.INSTALL_SHORTCUT" );
                    if ( permissionCheck != PackageManager.PERMISSION_GRANTED ) {
                        // CANCEL SHORTCUT
                        return;
                    }

                    // Tracking
                    Smartpush.hit( mContext, pushId, null, null, SmartpushHitUtils.Action.INSTALLED, null);
                    addShortcut( data );

                } else if ( "LOOPBACK".equals( adType ) ) {
                    // Tracking
                    Smartpush.hit( mContext, pushId, null, null, SmartpushHitUtils.Action.ONLINE, null );
                } else {
                    // Create Notification
                    createNotification( data );
                }
            } else {
                // by pass
                if ( mContext instanceof SmartpushListenerService ) {
                    ( ( SmartpushListenerService )mContext ).handleMessage( data );
                }
            }

        }
    }

    // Build Notification
    private void createNotification( Bundle extras ) {

        if ( extras.containsKey( VIDEO_URI ) ) {
            // Prefetching video...
            String midiaId =
                    extras.getString( VIDEO_URI, null );

            CacheManager
                    .getInstance( mContext )
                    .prefetchVideo( midiaId, CacheManager.ExpirationTime.NONE );
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder( mContext )
                .setSmallIcon(getPushIcon(extras))               // Set Small Icon
                .setAutoCancel( isAutoCancel( extras ) )         // Set Auto Cancel Action
                .setContentIntent( addMainAction( extras ) )     // Set Main Action
                .setContentTitle ( extras.getString( TITLE ) )   // Set Title
                .setContentText  ( extras.getString( DETAIL ) )  // Set 2nd line
                .setWhen( System.currentTimeMillis() )           // Set WHEN ARRIVE
                .setLights( Color.GREEN, 1000, 5000 )            // Set LIGHT Color and pattern
                .setPriority( NotificationCompat.PRIORITY_HIGH );

        if ( vibrate( extras ) ) {                               // VIBRATE
            builder.setVibrate(  new long[] { 100, 500, 200, 800 } );
        }

        addSecondaryActions( extras, builder );                  // Set Secondary Actions

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
            NotificationCompat.BigPictureStyle style = createBigPictureStyle( extras );
            if ( style != null ) {
                builder.setStyle( style );                      // Set Big Banner
            }
        }

        setBigIcon( extras, builder );             // Set Large Icon

        NotificationManagerCompat nm = NotificationManagerCompat.from( mContext );
        nm.cancel( PUSH_INTERNAL_ID );
        nm.notify( PUSH_INTERNAL_ID, builder.build() );

    }

    private int getPushIcon( Bundle extras ) {
        int category = Integer.parseInt( getValue( extras.getString( CATEGORY ), "1" )  ) ;
        category = ( category >= pushIcons.length ) ? 1 : category;
        return pushIcons[ category - 1 ];
    }

    private void setBigIcon( Bundle extras, NotificationCompat.Builder builder ) {
        String urlpath = extras.getString( LAUNCH_ICON );

        Resources resources = mContext.getResources();

        if ( urlpath == null ) {
            int category =
                    Integer.parseInt( getValue( extras.getString( CATEGORY ), "1" ) ) ;

            category = ( category >= pushIcons.length ) ? 1 : category;

            if ( category == CATEGORY_BUSCAPE ) {
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
        return ( "0".equals( extras.getString( AUTO_CANCEL ) ) ) ? false : true;
    }

    private boolean vibrate( Bundle extras ) {
        if ( ( "1".equals( extras.getString( VIBRATE ) ) ) ? true : false ) {
//            int result = this.checkCallingOrSelfPermission( Manifest.permission.VIBRATE );
            int permissionCheck = ContextCompat.checkSelfPermission( mContext,
                    Manifest.permission.VIBRATE );
            return ( permissionCheck == PackageManager.PERMISSION_GRANTED );
        }

        return false;
    }

    private PendingIntent addMainAction(Bundle extras ) {
        String action = extras.getString( URL );

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
            if ( !extras.containsKey( VIDEO_URI ) ) {
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
//                    count++;
                }
            }
        }
    }

    private NotificationCompat.BigPictureStyle createBigPictureStyle( Bundle extras ) {
        Bitmap bitmap =
                CacheManager
                        .getInstance( mContext )
                        .loadBitmap( extras.getString( NOTIF_BANNER ), CacheManager.ExpirationTime.DAY );

        if ( bitmap == null ) return null;

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
                .setBigContentTitle(extras.getString(TITLE))
                .setSummaryText( extras.getString( DETAIL ) )
                .bigPicture( resizedBitmap );

        return notiStyle;
    }

    private void addShortcut( Bundle extras ) {
        //Adding shortcut for MainActivity
        //on Home screen
        Intent shortcutIntent;
        if ( extras.getString( URL ).startsWith( "market://details?id=" ) ) {
            shortcutIntent = new Intent( Intent.ACTION_VIEW );
            shortcutIntent.setData( Uri.parse(extras.getString(URL)) );
        } else {
            shortcutIntent = new Intent( mContext, SmartpushActivity.class );
            shortcutIntent
                    .putExtras( extras )
                    .addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
            shortcutIntent.setAction( Intent.ACTION_MAIN );
        }

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent );
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, extras.getString( TITLE ) );

//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
//                Intent.ShortcutIconResource.fromContext( getApplicationContext(), R.drawable.ic_launcher ) );

        int size = ( int ) mContext.getResources().getDimension( android.R.dimen.app_icon_size );

        if ( extras.getString( LAUNCH_ICON ) != null ) {
            Bitmap b =
                    CacheManager
                            .getInstance( mContext )
                            .loadBitmap(extras.getString(LAUNCH_ICON), CacheManager.ExpirationTime.DAY );

            if ( b != null ) {
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, Bitmap.createScaledBitmap(b, size, size, false));
            } else {
                return;
            }

        }

        addIntent.setAction( "com.android.launcher.action.INSTALL_SHORTCUT" );
        mContext.sendBroadcast( addIntent );
    }
}