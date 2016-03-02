package comm.sjun.mygooglefit3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tbruyelle.rxpermissions.RxPermissions;

import rx.functions.Action1;

/**
 * Created by user on 2016-02-26.
 */
public class PermissionActivity extends Activity {
    public static final String PERMISSION = "permission";
    public static final String ACTIVITY = "activity";
    public static final String INTENT = "intent";
    private static final String ARGUMENT = "argument_intent";

    private static final String TAG = PermissionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: start");

        final Intent intent = getIntent().getParcelableExtra(ARGUMENT);

        RxPermissions rxPermissions = RxPermissions.getInstance(this);
        rxPermissions.request(intent.getStringExtra(PERMISSION))
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        if (granted) {
                            backToOriginalActivity((Intent) intent.getParcelableExtra(INTENT));
                        }

                        finish();
                    }
                });
    }

    private void backToOriginalActivity(Intent intent) {
        Intent newIntent = new Intent();
        newIntent.setComponent(intent.getComponent());
        newIntent.putExtras(intent);

        this.startActivity(newIntent);
    }

    public static void startActivity(Activity activity, Intent intent) {
        Intent newIntent = new Intent(activity, PermissionActivity.class);
        newIntent.putExtra(ARGUMENT, intent);
        activity.startActivity(newIntent);
    }
}
