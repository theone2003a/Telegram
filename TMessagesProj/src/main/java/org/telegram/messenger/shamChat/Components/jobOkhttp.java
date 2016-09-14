package org.telegram.messenger.shamChat.Components;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

/**
 * Created by Msa on 9/15/16.
 */
public class jobOkhttp extends Job {
   msaOkHttp okHttp ;
    public static final int PRIORITY = 1000;

    protected jobOkhttp(msaOkHttp okHttp) {
        super(new Params(PRIORITY).persist().requireNetwork());
        this.okHttp = okHttp ;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        okHttp.run();
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
