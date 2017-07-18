package br.com.smartpush;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by fabio.licks on 02/09/15.
 */
class SmartpushFragmentWebView extends Fragment {

    private ProgressBar progress;
    private WebView      webview;
    private TextView  brandGetmo;

    private Animation    fadeOut;
    private Animation     fadeIn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //
        fadeOut = new AlphaAnimation( 1, 0 );
        fadeOut.setInterpolator( new AccelerateInterpolator() );
        fadeOut.setDuration( 500 );

        //
        fadeIn = new AlphaAnimation( 0, 1 );
        fadeIn.setInterpolator( new DecelerateInterpolator() );
        fadeIn.setDuration( 250 );
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        //
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );

        RelativeLayout parent = new RelativeLayout( getActivity() );
        parent.setLayoutParams(params);
        parent.setBackgroundColor( Color.WHITE );

        // ImageView
        params =
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
        params.addRule( RelativeLayout.CENTER_IN_PARENT );

        // GETMO LOGO
        brandGetmo = new TextView( getActivity() );
        brandGetmo.setLayoutParams(params);
        brandGetmo.setText("www.getmo.com.br");
        brandGetmo.setTextSize(24);
        brandGetmo.setTextColor(Color.parseColor("#DDDDDD"));

        parent.addView(brandGetmo);

        // webview
        webview = new WebView( getActivity() );
        webview.getSettings().setJavaScriptEnabled( true );

        webview.setWebChromeClient( new WebChromeClient() {
            public void onProgressChanged( WebView view, int progress ) {
                SmartpushFragmentWebView.this.setValue( progress );
            }
        } );

        webview.setWebViewClient( new WebViewClient() {
            @Override
            public void onPageFinished( WebView view, String url ) {
                super.onPageFinished( view, url );

                brandGetmo.startAnimation(fadeOut);
                brandGetmo.setVisibility(View.GONE);

                webview.startAnimation( fadeIn );
                webview.setVisibility( View.VISIBLE );

                progress.setVisibility( View.GONE );
            }

            @Override
            public void onReceivedError( WebView view, int errorCode, String description, String failingUrl ) {
                super.onReceivedError( view, errorCode, description, failingUrl );
                getActivity().finish();
            }
        });

        parent.addView( webview );

        // progressbar
        params =
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ( int ) ( 3 * getResources().getDisplayMetrics().density ) );

        params.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM );

        progress = new ProgressBar( getActivity(), null, android.R.attr.progressBarStyleHorizontal );
        progress.setLayoutParams( params );
        progress.setMax( 100 );

        parent.addView( progress );

        if ( SmartpushHttpClient.isConnected(getActivity()) ) {
            load();
        } else {
            Toast.makeText(
                    getActivity(),
                    getString( R.string.smartp_noconnection ), Toast.LENGTH_SHORT ).show();
            getActivity().finish();
        }

        return parent;
    }

    public void setValue( int progress ) {
        this.progress.setProgress(progress);
    }

    private void load() {
        resetScreen();
        final Bundle extras = getArguments();
        if ( extras != null ) {
            webview.loadUrl( extras.getString( SmartpushListenerService.URL ) );
        } else {
            getActivity().finish();
        }
    }

    private void resetScreen() {
        progress.setProgress( 0 );
        webview.startAnimation( fadeOut );
        webview.setVisibility( View.GONE );
        brandGetmo.startAnimation(fadeIn);
        brandGetmo.setVisibility(View.VISIBLE);
        progress.setVisibility( View.VISIBLE );
    }
}
