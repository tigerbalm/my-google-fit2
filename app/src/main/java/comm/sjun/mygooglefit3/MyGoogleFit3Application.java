package comm.sjun.mygooglefit3;

import android.app.Application;
import android.util.Log;

import timber.log.Timber;

/**
 * Created by user on 2016-01-20.
 */
public class MyGoogleFit3Application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ErrorTree());
        }
    }

    private static class ErrorTree extends Timber.Tree {
        private static final String TAG = "MyGoogleFit";

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.ERROR) {
                Log.e(TAG, message, t);
            }
        }
    }
}
