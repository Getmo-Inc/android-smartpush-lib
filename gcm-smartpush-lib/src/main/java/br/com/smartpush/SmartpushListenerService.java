package br.com.smartpush;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;
import java.net.MalformedURLException;

import br.com.smartpush.u.SmartpushHitUtils;
import br.com.smartpush.u.SmartpushHttpClient;
import br.com.smartpush.u.SmartpushIntentUtils;

import static br.com.smartpush.u.SmartpushUtils.ONLY_PORTRAIT;
import static br.com.smartpush.u.SmartpushUtils.TAG;
import static br.com.smartpush.u.SmartpushUtils.getValue;

/**
 * Created by fabio.licks on 10/02/16.
 */
public abstract class SmartpushListenerService extends GcmListenerService {

    // Push metadata
    public static String TITLE        = "title";
    public static String DETAIL       = "detail";
    public static String URL          = "url";
    public static String VIDEO_URI    = "video";
    public static String AUTO_CANCEL  = "ac";
    public static String VIBRATE      = "vib";

    public static String NOTIF_BANNER = "banner";
    public static String LAUNCH_ICON  = "icon";

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
            R.drawable.ic_animais
    };

    private static final int PUSH_INTERNAL_ID = 0;


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived( String from, Bundle data ) {

        if ( data != null && !data.isEmpty() ) {  // has effect of unparcelling Bundle

            String adType = data.getString( "adtype" );
            String adNet  = data.getString( "adnetwork" );

            // Tracking
            String pushId = SmartpushHitUtils
                    .getValueFromPayload(SmartpushHitUtils.Fields.PUSH_ID, data);
            Smartpush.hit( this, pushId, null, null, SmartpushHitUtils.Action.RECEIVED, null);

            if ( "smartpush".equals( adNet ) ) {
                if ( "ICON_AD".equals( adType ) ) {

                    int result = this.checkCallingOrSelfPermission(
                            "com.android.launcher.permission.INSTALL_SHORTCUT" );
                    if ( result != PackageManager.PERMISSION_GRANTED ) {
                        // CANCEL SHORTCUT
                        return;
                    }

                    // Tracking
                    Smartpush.hit(this, pushId, null, null, SmartpushHitUtils.Action.INSTALLED, null);

                    addShortcut( data );

                } else if ( "LOOPBACK".equals( adType ) ) {
                    // Tracking
                    Smartpush.hit(this, pushId, null, null, SmartpushHitUtils.Action.ONLINE, null);
                } else {
                    // Create Notification
                    createNotification(data);
                }
            } else {
                // by pass
                handleMessage( data );
            }

        }
        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        // sendNotification(message);
        // [END_EXCLUDE]
    }

    private int getPushIcon( Bundle extras ) {
        int category = Integer.parseInt( getValue(extras.getString("category"), "0")  ) ;
        return pushIcons[ category - 1 ];
    }

    // Build Notification
    private void createNotification(Bundle extras) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder( this )
                .setSmallIcon(getPushIcon(extras))           // Set Small Icon
                .setAutoCancel( isAutoCancel( extras ) )         // Set Auto Cancel Action
                .setContentIntent( addMainAction( extras ) )     // Set Main Action
                .setContentTitle ( extras.getString( TITLE ) )  // Set Title
                .setContentText  ( extras.getString( DETAIL ) ) // Set 2nd line
                .setWhen( System.currentTimeMillis() )           // Set WHEN ARRIVE
                .setLights( Color.GREEN, 1000, 5000 )            // Set LIGHT Color and pattern
                .setPriority( NotificationCompat.PRIORITY_HIGH );

        if ( vibrate( extras ) ) {                           // VIBRATE
            builder.setVibrate(  new long[] { 100, 500, 200, 800 } );
        }

        addSecondaryActions( extras, builder );              // Set Secondary Actions

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
            NotificationCompat.BigPictureStyle style = createBigPictureStyle( extras );
            if ( style != null ) {
                builder.setStyle( style );                  // Set Big Banner
            }
        }

        loadNotificationBigIcon( extras, builder );         // Set Large Icon

        NotificationManagerCompat nm = NotificationManagerCompat.from( this );
        nm.cancel( PUSH_INTERNAL_ID );
        nm.notify( PUSH_INTERNAL_ID, builder.build() );

    }

    private void loadNotificationBigIcon( Bundle extras, NotificationCompat.Builder builder ) {
        String urlpath = extras.getString( LAUNCH_ICON );
        if ( urlpath == null ) return;

        try {
//			int h = ( int ) getResources().getDimension( android.R.dimen.notification_large_icon_height );
//			int w = ( int ) getResources().getDimension( android.R.dimen.notification_large_icon_width );

            float scaleFactor = getResources().getDisplayMetrics().density;
            int size = ( int ) ( 24 * scaleFactor + 0.5f );

            Bitmap b = SmartpushHttpClient.loadBitmap(urlpath);
            if ( b != null ) {
                builder.setLargeIcon( Bitmap.createScaledBitmap( b, size, size, false ) );
            }
        } catch ( IOException e) {
            Log.e( TAG, e.getMessage(), e );
        }
    }

//    private int genNextId() {
////    	Random randomGenerator = new Random( System.currentTimeMillis() );
////    	return randomGenerator.nextInt( 1000 );
//        return 0;
//    }

    private boolean isAutoCancel( Bundle extras ) {
        return ( "0".equals( extras.getString( AUTO_CANCEL ) ) ) ? false : true;
    }

    private boolean vibrate( Bundle extras ) {
        if ( ( "1".equals( extras.getString( VIBRATE ) ) ) ? true : false ) {
            int result = this.checkCallingOrSelfPermission( Manifest.permission.VIBRATE );
            return ( result == PackageManager.PERMISSION_GRANTED );
        }

        return false;
    }

    private PendingIntent addMainAction( Bundle extras ) {
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
            it = new Intent( getApplicationContext(), SmartpushActivity.class );
            if ( !extras.containsKey( VIDEO_URI ) ) {
                extras.putBoolean( ONLY_PORTRAIT, true );  // Lock screen orientation
            }
            it.putExtras( extras ).addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
        }

        // Creates and return the PendingIntent
        return PendingIntent.getActivity( this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    private void addSecondaryActions( Bundle extras, NotificationCompat.Builder builder ) {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
            int count = 0;

            String act = extras.getString( "CALL" );
            if ( act != null ) {
                builder.addAction( R.drawable.ic_call, getString( R.string.call ),
                        PendingIntent.getActivity( this, 0, SmartpushIntentUtils.dialPhone( act ), 0 ) );
                count++;
            }

            act = extras.getString( "SMS" );
            if ( act != null ) {
                String[] params = act.split( ";" );
                if ( params.length == 2 ) {
                    builder.addAction( R.drawable.ic_sms, getString( R.string.sms ),
                            PendingIntent
                                    .getActivity(
                                            this, 0, SmartpushIntentUtils.sendSms(
                                                    this, params[0], params[1]), 0) );
                    count++;
                }
            }

            act = extras.getString( "EMAIL" );
            if ( act != null ) {
                String[] params = act.split( ";" );
                if ( params.length == 3 ) {
                    builder.addAction( R.drawable.ic_email, getString( R.string.email ),
                            PendingIntent
                                    .getActivity(
                                            this, 0, SmartpushIntentUtils.sendEmail(
                                                    params[0], params[1], params[2]), 0) );
                    count++;
                }
            }

            act = extras.getString( "MAPS" );
            if ( act != null && count < 3 ) {
                String[] params = act.split( ";" );
                if ( params.length == 2 ) {
                    builder.addAction( R.drawable.ic_mapas, getString( R.string.map ),
                            PendingIntent
                                    .getActivity(
                                            this, 0, SmartpushIntentUtils.showLocation(
                                                    Float.parseFloat(params[0]), Float.parseFloat(params[1]), 13), 0) );
                    count++;
                }
            }

            act = extras.getString( "SHARE" );
            if ( act != null && count < 3 ) {
                String[] params = act.split( ";" );
                if ( params.length == 2 ) {
                    builder.addAction( R.drawable.ic_compartilhar, getString( R.string.share ),
                            PendingIntent.getActivity( this, 0, SmartpushIntentUtils.shareText( params[ 0 ], params[ 1 ] ), 0 ) );
//                    count++;
                }
            }
        }
    }

    private NotificationCompat.BigPictureStyle createBigPictureStyle( Bundle extras ) {
        Bitmap bitmap;

        try {

            bitmap = SmartpushHttpClient.loadBitmap(extras.getString(NOTIF_BANNER));

        } catch ( MalformedURLException e1 ) {
            Log.e( TAG, e1.getMessage(), e1 );
            return null;
        } catch (IOException e1) {
            Log.e( TAG, e1.getMessage(), e1 );
            return null;
        }

        if ( bitmap == null ) return null;

        // Resize picture
        WindowManager wm = ( WindowManager) getApplicationContext().getSystemService( Context.WINDOW_SERVICE );
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics( metrics );

        int imageWidth  = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        int newWidth    = 400; //metrics.widthPixels;

        float scaleFactor = ( float ) newWidth / ( float ) imageWidth;

        int newHeight = ( int )( imageHeight * scaleFactor );

        Log.i( TAG, "Picture size: " + newWidth + "," + newHeight );

        Bitmap resizedBitmap = Bitmap.createScaledBitmap( bitmap, newWidth, newHeight, true );

        // Big Picture Style config
        NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle()
                .setBigContentTitle(extras.getString(TITLE))
                .setSummaryText( extras.getString( DETAIL ) )
                .bigPicture( resizedBitmap );

        return notiStyle;
    }

    protected abstract void handleMessage( Bundle data );

    private void addShortcut( Bundle extras ) {
        //Adding shortcut for MainActivity
        //on Home screen
        Intent shortcutIntent;
        if ( extras.getString( URL ).startsWith( "market://details?id=" ) ) {
            shortcutIntent = new Intent( Intent.ACTION_VIEW );
            shortcutIntent.setData( Uri.parse(extras.getString(URL)) );
        } else {
            shortcutIntent = new Intent( getApplicationContext(), SmartpushActivity.class );
            shortcutIntent.putExtras( extras ).addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
            shortcutIntent.setAction( Intent.ACTION_MAIN );
        }

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent );
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, extras.getString( TITLE ) );

//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
//                Intent.ShortcutIconResource.fromContext( getApplicationContext(), R.drawable.ic_launcher ) );

        int size = ( int ) getResources().getDimension( android.R.dimen.app_icon_size );

        if ( extras.getString( LAUNCH_ICON ) != null ) {
            Bitmap b;
            try {
                b = SmartpushHttpClient.loadBitmap(extras.getString(LAUNCH_ICON));
                if ( b != null ) {
                    addIntent.putExtra( Intent.EXTRA_SHORTCUT_ICON, Bitmap.createScaledBitmap( b, size, size, false ) );
                }
            } catch ( MalformedURLException e ) {
                Log.e( TAG, e.getMessage(), e );
            } catch ( IOException e) {
                Log.e( TAG, e.getMessage(), e );
            }
        }

        addIntent.setAction( "com.android.launcher.action.INSTALL_SHORTCUT" );
        getApplicationContext().sendBroadcast( addIntent );
    }
}
