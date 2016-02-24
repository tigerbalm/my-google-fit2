package comm.sjun.mygooglefit3;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * Created by user on 2016-01-20.
 */
public class JavaScriptBridge {
    private final WebView webview;
    private final Context context;

    public JavaScriptBridge(Context context, WebView webView) {
        this.context = context;
        this.webview = webView;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void fetchData(long date) {
        // start event loading
    }

    @JavascriptInterface
    public void fetchDataRange(long start, long end) {
        // start event loading
    }

    @JavascriptInterface
    public void refreshView(String json) {
        // update current html with json data
    }

    @JavascriptInterface
    public String getWorkoutDataOn(long timemillis) {
        return "[{\n" +
                "            \"value\": 300,\n" +
                "            \"color\":\"#F7464A\",\n" +
                "            \"highlight\": \"#FF5A5E\",\n" +
                "            \"label\": \"Walking\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"value\": 50,\n" +
                "            \"color\": \"#46BFBD\",\n" +
                "            \"highlight\": \"#5AD3D1\",\n" +
                "            \"label\": \"Running\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"value\": 100,\n" +
                "            \"color\": \"#FDB45C\",\n" +
                "            \"highlight\": \"#FFC870\",\n" +
                "            \"label\": \"Cycling\"\n" +
                "        }]";
    }

    @JavascriptInterface
    public void notifyDataSetChanged() {
        webview.loadUrl("javascript:refresh()");
    }
}
