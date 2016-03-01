package comm.sjun.mygooglefit3;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;

import butterknife.Bind;
import butterknife.ButterKnife;
import comm.sjun.mygooglefit3.api.GoogleApiClientBridge;
import comm.sjun.mygooglefit3.data.WorkoutDB;
import comm.sjun.mygooglefit3.util.Constants;
import rx.functions.Action1;
import timber.log.Timber;

import static java.text.DateFormat.getDateInstance;

// TODO:
// 1. add permission handling
// 2. add load data to client bridge
// 3. add observable of WorkoutRecord
// 4. add provider for workdout db
public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    @Bind(R.id.webview)
    WebView webView;

    private GoogleApiClientBridge googleApiClientBridge;
    private String googleApiClientToken;
    private boolean shouldAutoLogin = true;
    private JavaScriptBridge javascriptBridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        RxPermissions rxPermissions = RxPermissions.getInstance(this);

        if (!rxPermissions.isGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            startPermissionActivity(android.Manifest.permission.ACCESS_FINE_LOCATION);
            finish();
            return;
        }

        initialize();
    }

    private void startPermissionActivity(String permission) {
        Intent intent = new Intent();
        intent.putExtra(PermissionActivity.INTENT, getIntent());
        intent.putExtra(PermissionActivity.PERMISSION, permission);
        //options.putSerializable(PermissionActivity.ACTIVITY, MainActivity.class);

        PermissionActivity.startActivity(this, intent);
    }

    private void initialize() {
        googleApiClientBridge = new GoogleApiClientBridge();
        googleApiClientToken = googleApiClientBridge.init(this, this, this);

        javascriptBridge = new JavaScriptBridge(this, webView);

        setWebView();
    }

    private void setWebView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // web setting
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);

        webView.addJavascriptInterface(javascriptBridge, "Android");
        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("MyGoogleFit2", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });

        // load html
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClientBridge.connect(googleApiClientToken);
    }

    @Override
    public void onStop() {
        googleApiClientBridge.disconnect(googleApiClientToken);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (googleApiClientBridge != null) {
            googleApiClientBridge.destroy(googleApiClientToken);
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult:%d:%d:%s", requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQUEST_CODE_GOOGLE_PLUS_SIGN_IN:
                GoogleSignInResult result = googleApiClientBridge.getSignInResultFromIntent(data);
                handleSignInResult(result);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void handleSignInResult(GoogleSignInResult result) {
        Timber.d("handleSignInResult status: %s", result.getStatus());
        if (result.isSuccess()) {
            googleApiClientBridge.setCurrentAccount(result.getSignInAccount());

            // TODO: start load-data and update webview
            // TODO: permission handling
            updateWebView();
        } else {
            googleApiClientBridge.setCurrentAccount(null);

            Intent signInIntent = googleApiClientBridge.getSignInIntent(googleApiClientToken);
            startActivityForResult(signInIntent, Constants.REQUEST_CODE_GOOGLE_PLUS_SIGN_IN);
        }
    }

    private void updateWebView() {
        // read 1 week data
        new ReadSessionTask().execute();
        // update view
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("onConnected");
        if (shouldAutoLogin) {
            OptionalPendingResult<GoogleSignInResult> pendingResult =
                    googleApiClientBridge.silentSignIn(googleApiClientToken);
            if (pendingResult.isDone()) {
                // There's immediate result available.
                handleSignInResult(pendingResult.get());
            } else {
                // There's no immediate result ready
                pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult result) {
                        handleSignInResult(result);
                    }
                });
            }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Timber.d("onConnectionSuspended: %d", cause);
        googleApiClientBridge.connect(googleApiClientToken);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Timber.d("onConnectionFailed: %s", connectionResult);
    }

    private class ReadSessionTask extends AsyncTask<Void, Void, DataReadResult> {

        @Override
        protected DataReadResult doInBackground(Void... params) {
            // Begin by creating the query.
            DataReadRequest readRequest = queryFitnessData();

            // [START read_session]
            // Invoke the Sessions API to fetch the session with the query and wait for the result
            // of the read request. Note: Fitness.SessionsApi.readSession() requires the
            // ACCESS_FINE_LOCATION permission.
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(googleApiClientBridge.getClient(googleApiClientToken),
                            readRequest)
                            .await(1, TimeUnit.MINUTES);

            // For the sake of the sample, we'll print the data so we can see what we just added.
            // In general, logging fitness information should be avoided for privacy reasons.
            //printData(dataReadResult);

            // memory db
            WorkoutDB.setData(dataReadResult);

            return dataReadResult;
        }

        private DataReadRequest queryFitnessData() {
            Timber.d("readFitnessSession: ");


            // [START build_read_session_request]
            // Set a start and end time for our query, using a start time of 1 week before this moment.
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            long startTime = cal.getTimeInMillis();

            java.text.DateFormat dateFormat = getDateInstance();
            Timber.d("Range Start: " + dateFormat.format(startTime));
            Timber.d("Range End: " + dateFormat.format(endTime));

            DataReadRequest readRequest = new DataReadRequest.Builder()
                    // The data request can specify multiple data types to return, effectively
                    // combining multiple data queries into one call.
                    // In this example, it's very unlikely that the request is for several hundred
                    // datapoints each consisting of a few steps and a timestamp.  The more likely
                    // scenario is wanting to see how many steps were walked per day, for 7 days.
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                    .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                    .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                    // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                    // bucketByTime allows for a time span, whereas bucketBySession would allow
                    // bucketing by "sessions", which would need to be defined in code.
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build();
            // [END build_read_data_request]

            return readRequest;
        }

        /**
         * Log a record of the query result. It's possible to get more constrained data sets by
         * specifying a data source or data type, but for demonstrative purposes here's how one would
         * dump all the data. In this sample, logging also prints to the device screen, so we can see
         * what the query returns, but your app should not log fitness information as a privacy
         * consideration. A better option would be to dump the data you receive to a local data
         * directory to avoid exposing it to other applications.
         */
        private void printData(DataReadResult dataReadResult) {
            // [START parse_read_data_result]
            // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
            // as buckets containing DataSets, instead of just DataSets.
            if (dataReadResult.getBuckets().size() > 0) {
                Timber.d("Number of returned buckets of DataSets is: "
                        + dataReadResult.getBuckets().size());
                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    Timber.d("Number of returned DataSets of buckets is: " + dataSets.size());
                    for (DataSet dataSet : dataSets) {
                        dumpDataSet(dataSet);
                    }
                }
            } else if (dataReadResult.getDataSets().size() > 0) {
                Timber.d("Number of returned DataSets is: "
                        + dataReadResult.getDataSets().size());
                for (DataSet dataSet : dataReadResult.getDataSets()) {
                    dumpDataSet(dataSet);
                }
            }
            // [END parse_read_data_result]
        }

        // [START parse_dataset]
        private void dumpDataSet(DataSet dataSet) {
            Timber.d("Data returned for Data type: " + dataSet.getDataType().getName());
            DateFormat dateFormat = getDateInstance();

            for (DataPoint dp : dataSet.getDataPoints()) {
                Timber.d("Data point:");
                Timber.d("\tType: " + dp.getDataType().getName());
                Timber.d("\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                Timber.d("\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                for (Field field : dp.getDataType().getFields()) {
                    Timber.d("\tField: " + field.getName() +
                            " Value: " + dp.getValue(field));
                }
            }
        }
        // [END parse_dataset]

        @Override
        protected void onPostExecute(DataReadResult dataReadResult) {
            super.onPostExecute(dataReadResult);

            javascriptBridge.notifyDataSetChanged();
        }

    }
}
