package br.com.smartpush;

import android.graphics.Bitmap;

/**
 * Created by fabio.licks on 22/09/17.
 */

class SlideInfo {

    public String url;
    public String packageName;
    public Bitmap bitmap;

    @Override
    public String toString() {
        return "SlideInfo{" +
                "url='" + url + '\'' +
                ", packageName='" + packageName + '\'' +
                ", bitmap=" + ( bitmap != null ) + '}';
    }
}
