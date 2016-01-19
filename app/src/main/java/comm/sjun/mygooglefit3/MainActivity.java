package comm.sjun.mygooglefit3;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import comm.sjun.mygooglefit3.api.GoogleApiClientBridge;
import comm.sjun.mygooglefit3.util.Constants;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    @Bind(R.id.webview)
    WebView webView;

    private GoogleApiClientBridge googleApiClientBridge;
    private String googleApiClientToken;
    private boolean shouldAutoLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        googleApiClientBridge = new GoogleApiClientBridge();
        googleApiClientToken = googleApiClientBridge.init(this, this, this);

        setWebView();
    }

    private void setWebView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // web setting
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
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

    @Override public void onStart() {
        super.onStart();
        googleApiClientBridge.connect(googleApiClientToken);
    }

    @Override public void onStop() {
        googleApiClientBridge.disconnect(googleApiClientToken);
        super.onStop();
    }

    @Override public void onDestroy() {
        googleApiClientBridge.destroy(googleApiClientToken);
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
        } else {
            googleApiClientBridge.setCurrentAccount(null);

            Intent signInIntent = googleApiClientBridge.getSignInIntent(googleApiClientToken);
            startActivityForResult(signInIntent, Constants.REQUEST_CODE_GOOGLE_PLUS_SIGN_IN);
        }
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
}
