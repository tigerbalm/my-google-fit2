package comm.sjun.mygooglefit3;

import android.app.Activity;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import comm.sjun.mygooglefit3.data.ActivityType;
import comm.sjun.mygooglefit3.data.StepsByDay;
import comm.sjun.mygooglefit3.data.StepsByHour;
import comm.sjun.mygooglefit3.data.StepsSummary;
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
    private final Gson gson;

    public JavaScriptBridge(Context context, WebView webView) {
        this.context = context;
        this.webview = webView;

        this.gson = new Gson();
    }

    @JavascriptInterface
    public void getTodayTotal(String callback) {
        Log.d(TAG, "getTodayTotal: start");

        DataReadRequest.Builder dataReadRequestBuilder = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                //.aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(LocalDate.getInstance().getMillis(),
                        System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        DataReadRequest dataReadRequest = dataReadRequestBuilder.build();
        DataReadRequest dataReadRequestServer = dataReadRequestBuilder.enableServerQueries().build();

        RxFit.History.read(dataReadRequest)
                .doOnNext(dataReadResult -> Timber.d("start history read"))
//                .doOnError(new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        if (throwable instanceof StatusException
//                                && ((StatusException) throwable).getStatus().getStatusCode() == CommonStatusCodes.TIMEOUT) {
//                            Log.e(TAG, "Timeout on server query request");
//                        }
//                    }
//                })
//                .compose(new RxFit.OnExceptionResumeNext<>(RxFit.History.read(dataReadRequest)))
                .flatMap(new Func1<DataReadResult, Observable<? extends Bucket>>() {
                    @Override
                    public Observable<? extends Bucket> call(DataReadResult dataReadResult) {
                        return Observable.from(dataReadResult.getBuckets());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(bucket1 -> bucket1 != null)
                .subscribe(bucket -> {
                    dump(bucket);

                    StepsSummary[] summaries = toStepsSummary(bucket);

                    String argument = gson.toJson(summaries);
                    Timber.d(argument);

//                    String argument =
//                            "[\n" +
//                                    "        {\n" +
//                                    "            \"value\": 300,\n" +
//                                    "            \"color\":\"#F7464A\",\n" +
//                                    "            \"highlight\": \"#FF5A5E\",\n" +
//                                    "            \"label\": \"Walking\"\n" +
//                                    "        },\n" +
//                                    "        {\n" +
//                                    "            \"value\": 50,\n" +
//                                    "            \"color\": \"#46BFBD\",\n" +
//                                    "            \"highlight\": \"#5AD3D1\",\n" +
//                                    "            \"label\": \"Running\"\n" +
//                                    "        },\n" +
//                                    "        {\n" +
//                                    "            \"value\": 100,\n" +
//                                    "            \"color\": \"#FDB45C\",\n" +
//                                    "            \"highlight\": \"#FFC870\",\n" +
//                                    "            \"label\": \"Cycling\"\n" +
//                                    "        }\n" +
//                                    "]";

                            webview.loadUrl("javascript:" + callback + "('" + argument + "')");
                });
    }

    private StepsSummary[] toStepsSummary(Bucket bucket) {
        List<StepsSummary> summaries = new ArrayList<>();

        for (DataSet set : bucket.getDataSets()) {
            for (DataPoint point : set.getDataPoints()) {
                StepsSummary summary = new StepsSummary(point.getValue(Field.FIELD_STEPS).asInt(), "walking");

                summaries.add(summary);
            }
        }

        return summaries.toArray(new StepsSummary[summaries.size()]);
    }

    private void dump(DataReadResult result) {
        Timber.d(result.toString());

        dump(result.getBuckets());
    }

    private void dump(List<Bucket> buckets) {
        for (Bucket bucket : buckets) {
            dump(bucket);
        }
    }

    private void dump(Bucket bucket) {
        Log.d(TAG, "dump: bucket-" + bucket);

        if (bucket.getSession() != null) {
            Log.d(TAG, "dump: bucket.getSession().getName()-" + bucket.getSession().getName());
            Log.d(TAG, "dump: bucket.getSession().getAppPackageName()-" + bucket.getSession().getAppPackageName());
            Log.d(TAG, "dump: bucket.getSession().getActivity()-" + bucket.getSession().getActivity());
            Log.d(TAG, "dump: bucket.getSession().getStartTime()-" + bucket.getSession().getStartTime(TimeUnit.MILLISECONDS));
            Log.d(TAG, "dump: bucket.getSession().getEndTime()-" + bucket.getSession().getEndTime(TimeUnit.MILLISECONDS));
        }

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
        DataReadRequest.Builder dataReadRequestBuilder = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(LocalDate.getInstance().add(-30).getMillis(),
                        System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        DataReadRequest dataReadRequest = dataReadRequestBuilder.build();
        DataReadRequest dataReadRequestServer = dataReadRequestBuilder.enableServerQueries().build();

        RxFit.History.read(dataReadRequest)
                .doOnNext(dataReadResult -> Timber.d("start history read"))
//                .doOnError(new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        if (throwable instanceof StatusException
//                                && ((StatusException) throwable).getStatus().getStatusCode() == CommonStatusCodes.TIMEOUT) {
//                            Log.e(TAG, "Timeout on server query request");
//                        }
//                    }
//                })
//                .compose(new RxFit.OnExceptionResumeNext<>(RxFit.History.read(dataReadRequest)))
                .map(dataReadResult -> dataReadResult.getBuckets())
                .map(buckets1 -> {
                    Collections.reverse(buckets1);
                    return buckets1;
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(bucket1 -> bucket1 != null)
                .subscribe(buckets -> {
                    dump(buckets);

                    StepsByDay[] steps = toStepsByDay(buckets);

                    String argument = gson.toJson(steps);
                    Timber.d(argument);

                    webview.loadUrl("javascript:" + callback + "('" + argument + "')");
                });
    }

    private StepsByDay[] toStepsByDay(List<Bucket> buckets) {
        List<StepsByDay> steps = new ArrayList<>();

        for (Bucket bucket : buckets) {
            for (DataSet set : bucket.getDataSets()) {
                for (DataPoint point : set.getDataPoints()) {
                    StepsByDay summary = new StepsByDay(toDateString(point), point.getValue(Field.FIELD_STEPS).asInt());

                    steps.add(summary);
                }
            }
        }

        return steps.toArray(new StepsByDay[steps.size()]);
    }

    private String toDateString(DataPoint point) {
        long time = point.getTimestamp(TimeUnit.MILLISECONDS);

        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);

        return DateFormat.getDateInstance().format(date.getTime());
    }

    @JavascriptInterface
    public void showDetail(int id, String page) {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webview.loadUrl("file:///android_asset/" + page + "?" + id);
            }
        });
    }

    @JavascriptInterface
    public void getWorkoutDetails(long millis, String callback) {
        DataReadRequest.Builder dataReadRequestBuilder = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(LocalDate.getInstance().setMillis(millis).getMillis(),
                        System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        DataReadRequest dataReadRequest = dataReadRequestBuilder.build();
        DataReadRequest dataReadRequestServer = dataReadRequestBuilder.enableServerQueries().build();

        RxFit.History.read(dataReadRequest)
                .doOnNext(dataReadResult -> Timber.d("start history read"))
//                .doOnError(new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        if (throwable instanceof StatusException
//                                && ((StatusException) throwable).getStatus().getStatusCode() == CommonStatusCodes.TIMEOUT) {
//                            Log.e(TAG, "Timeout on server query request");
//                        }
//                    }
//                })
//                .compose(new RxFit.OnExceptionResumeNext<>(RxFit.History.read(dataReadRequest)))
                .map(dataReadResult -> dataReadResult.getBuckets())
//                .map(buckets1 -> {
//                    Collections.reverse(buckets1);
//                    return buckets1;
//                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(bucket1 -> bucket1 != null)
                .subscribe(buckets -> {
                    dump(buckets);

                    StepsByHour[] steps = toStepsByHour(buckets);

                    String argument = gson.toJson(steps);
                    Timber.d(argument);

                    webview.loadUrl("javascript:" + callback + "('" + argument + "')");
                });
    }

    private StepsByHour[] toStepsByHour(List<Bucket> buckets) {
        List<StepsByHour> steps = new ArrayList<>();

        for (Bucket bucket : buckets) {
            StepsByHour info = new StepsByHour();

            for (DataSet set : bucket.getDataSets()) {
                for (DataPoint point : set.getDataPoints()) {
                    info.setTime(toHourString(point));

                    Timber.d("type: " + point.getDataType());
                    if (point.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                        info.setSteps(point.getValue(Field.FIELD_STEPS).asInt());
                    } else if (point.getDataType().equals(DataType.TYPE_DISTANCE_DELTA)) {
                        info.setDistance(point.getValue(Field.FIELD_DISTANCE).asFloat());
                    } else if (point.getDataType().equals(DataType.TYPE_CALORIES_EXPENDED)) {
                        info.setCalories(point.getValue(Field.FIELD_CALORIES).asFloat());
                    }
                }
            }

            steps.add(info);
        }

        return steps.toArray(new StepsByHour[steps.size()]);
    }

    private String toHourString(DataPoint point) {
        long time = point.getTimestamp(TimeUnit.MILLISECONDS);

        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);

        return "" + date.get(Calendar.HOUR_OF_DAY);
    }
}
