package comm.sjun.mygooglefit3;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.concurrent.TimeUnit;

import comm.sjun.mygooglefit3.util.LocalDate;
import timber.log.Timber;

/**
 * Created by sjun.lee on 2016-03-17.
 */
public class JavaScriptBridge2 implements IJavascriptInterface {
    private static final String TAG = JavaScriptBridge.class.getSimpleName();
    private final WebView webview;
    private final Context context;
    private final GoogleApiClient client;

    public JavaScriptBridge2(Context context, WebView webView, GoogleApiClient client) {
        this.context = context;
        this.webview = webView;
        this.client = client;
    }

    @Override
    @JavascriptInterface
    public void getTodayTotal(String callback) {
        DataReadRequest.Builder dataReadRequestBuilder = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketBySession(1, TimeUnit.DAYS)
                .setTimeRange(LocalDate.getInstance().getMillis(),
                        System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        DataReadRequest dataReadRequest = dataReadRequestBuilder.build();

        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(client, dataReadRequest).await(1, TimeUnit.MINUTES);

        dump(dataReadResult);


    }

    private void dump(DataReadResult result) {
        Timber.d(result.toString());

        for (Bucket bucket : result.getBuckets()) {
            dump(bucket);
        }
    }

    private void dump(Bucket bucket) {
        Timber.d("dump: bucket-" + bucket);

        Timber.d("dump: bucket.getSession().getName()-" + bucket.getSession().getName());
        Timber.d("dump: bucket.getSession().getAppPackageName()-" + bucket.getSession().getAppPackageName());
        Timber.d("dump: bucket.getSession().getActivity()-" + bucket.getSession().getActivity());
        Timber.d("dump: bucket.getSession().getStartTime()-" + bucket.getSession().getStartTime(TimeUnit.MILLISECONDS));
        Timber.d("dump: bucket.getSession().getEndTime()-" + bucket.getSession().getEndTime(TimeUnit.MILLISECONDS));

        for (DataSet set : bucket.getDataSets()) {
            Timber.d("dump: dataset -" + set);
            Timber.d("dump: dataset.DataType.name -" + set.getDataType().getName());
            Timber.d("dump: dataset.DataType.Field -" + set.getDataType().getFields());
            Timber.d("dump: dataset.DataPoints.size -" + set.getDataPoints().size());
            for (DataPoint point : set.getDataPoints()) {
                for (Field field : point.getDataType().getFields()) {
                    Timber.d("dump: dataset.DataPoints.field -" + field.getName());
                    Timber.d("dump: dataset.DataPoints.value -" + point.getValue(field));
                }
            }
        }
    }

    @Override
    @JavascriptInterface
    public void getAllWorkouts(String callback) {

    }

    @Override
    @JavascriptInterface
    public void showDetail(int id, String page) {

    }

    @Override
    @JavascriptInterface
    public void getWorkoutDetails(long millis, String callback) {

    }
}
