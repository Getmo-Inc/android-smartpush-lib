package br.com.smartpush;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import static br.com.smartpush.Utils.TAG;

public class SmartpushSubscribeJobScheduler extends JobService {
    final Handler workHandler = new Handler();
    Runnable workRunnable;

    @Override
    public boolean onStartJob( final JobParameters jobParameters ) {
        workRunnable = new Runnable() {
            @Override
            public void run() {
                // do your work here,
                // such as jobBackend.onStartJob(type, this)

                // TODO hack Oreo

                boolean reschedule = false;
                jobFinished( jobParameters, reschedule );
            }};
        workHandler.post( workRunnable );
        return true;
    }

    @Override
    public boolean onStopJob( JobParameters jobParameters ) {
        workHandler.removeCallbacks( workRunnable );
        return false;
    }


}
