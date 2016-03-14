package comm.sjun.mygooglefit3;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.patloew.rxfit.RxFit;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity2 extends AppCompatActivity {
    @Bind(R.id.webview)
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

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

        PermissionActivity.startActivity(this, intent);
    }

    private void initialize() {
        RxFit.init(this, new Api[] { Fitness.SESSIONS_API, Fitness.HISTORY_API }, new Scope[] { new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE) });
        RxFit.setDefaultTimeout(15, TimeUnit.SECONDS);

        initializeWebView();
    }

    private void initializeWebView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // web setting
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);

        webView.addJavascriptInterface(new JavaScriptBridge(this, webView), "Android");
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
}
