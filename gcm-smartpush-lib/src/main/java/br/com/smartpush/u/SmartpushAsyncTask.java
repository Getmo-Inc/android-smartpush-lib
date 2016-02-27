package br.com.smartpush.u;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

/**
 * Created by fabio.licks on 09/02/16.
 */

public abstract class SmartpushAsyncTask<T, V, Q> extends AsyncTask<T, V, Q>  {

    @SuppressWarnings("unchecked")
	@TargetApi( Build.VERSION_CODES.HONEYCOMB )
	public void goInParallel( T... content ) {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {        	
           this.executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR, content );           
        }
        else {        	
            this.execute( content );            
        }
    }
}