package comm.sjun.mygooglefit3;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.patloew.rxfit.RxFit;
import com.patloew.rxfit.StatusException;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import comm.sjun.mygooglefit3.data.ActivityType;
import comm.sjun.mygooglefit3.data.WorkoutDB;
import comm.sjun.mygooglefit3.util.LocalDate;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by user on 2016-01-20.
 */
public class JavaScriptBridge {
    private static final String TAG = JavaScriptBridge.class.getSimpleName();
    private final WebView webview;
    private final Context context;

    public JavaScriptBridge(Context context, WebView webView) {
        this.context = context;
        this.webview = webView;
    }

    @JavascriptInterface
    public void getTodayTotal(String callback) {
        Log.d(TAG, "getTodayTotal: start");

        DataReadRequest.Builder dataReadRequestBuilder = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketBySession(1, TimeUnit.DAYS)
                .setTimeRange(LocalDate.getInstance().getMillis(),
                        System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        DataReadRequest dataReadRequest = dataReadRequestBuilder.build();
        DataReadRequest dataReadRequestServer = dataReadRequestBuilder.enableServerQueries().build();

        RxFit.History.read(dataReadRequestServer)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof StatusException
                                && ((StatusException) throwable).getStatus().getStatusCode() == CommonStatusCodes.TIMEOUT) {
                            Log.e(TAG, "Timeout on server query request");
                        }
                    }
                })
                .compose(new RxFit.OnExceptionResumeNext<>(RxFit.History.read(dataReadRequest)))
                .flatMap(new Func1<DataReadResult, Observable<? extends Bucket>>() {
                    @Override
                    public Observable<? extends Bucket> call(DataReadResult dataReadResult) {
                        return Observable.from(dataReadResult.getBuckets());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bucket -> {
                    dump(bucket);
                });
    }

    private void dump(Bucket bucket) {
        Log.d(TAG, "dump: bucket-" + bucket);

        Log.d(TAG, "dump: bucket.getSession().getName()-" + bucket.getSession().getName());
        Log.d(TAG, "dump: bucket.getSession().getAppPackageName()-" + bucket.getSession().getAppPackageName());
        Log.d(TAG, "dump: bucket.getSession().getActivity()-" + bucket.getSession().getActivity());
        Log.d(TAG, "dump: bucket.getSession().getStartTime()-" + bucket.getSession().getStartTime(TimeUnit.MILLISECONDS));
        Log.d(TAG, "dump: bucket.getSession().getEndTime()-" + bucket.getSession().getEndTime(TimeUnit.MILLISECONDS));

        for (DataSet set : bucket.getDataSets()) {
            Log.d(TAG, "dump: dataset -" + set);
            Log.d(TAG, "dump: dataset.DataType.name -" + set.getDataType().getName());
            Log.d(TAG, "dump: dataset.DataType.Field -" + set.getDataType().getFields());
            Log.d(TAG, "dump: dataset.DataPoints.size -" + set.getDataPoints().size());
            for (DataPoint point : set.getDataPoints()) {
                for (Field field : point.getDataType().getFields()) {
                    Log.d(TAG, "dump: dataset.DataPoints.field -" + field.getName());
                    Log.d(TAG, "dump: dataset.DataPoints.value -" + point.getValue(field));
                }
            }
        }
    }

    @JavascriptInterface
    public void getAllWorkouts(String callback) {

    }

    @JavascriptInterface
    public void showDetail(int id, String page) {

    }

    @JavascriptInterface
    public void getWorkoutDetails(long millis, String callback) {

    }
}
