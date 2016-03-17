package comm.sjun.mygooglefit3;

import android.webkit.JavascriptInterface;

/**
 * Created by sjun.lee on 2016-03-17.
 */
public interface IJavascriptInterface {
    @JavascriptInterface
    void getTodayTotal(String callback);

    @JavascriptInterface
    void getAllWorkouts(String callback);

    @JavascriptInterface
    void showDetail(int id, String page);

    @JavascriptInterface
    void getWorkoutDetails(long millis, String callback);
}
