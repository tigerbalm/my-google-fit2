package comm.sjun.mygooglefit3.data;

import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

import auto.parcel.AutoParcel;

/**
 * Created by user on 2016-03-03.
 */
@AutoParcel
abstract public class ActivityType implements Parcelable {
    abstract String label();
    abstract long value();
    abstract String color();
    abstract String highlight();

    static ActivityType create(String label, long value, String color, String highlight) {
        return new AutoParcel_ActivityType(label, value, color, highlight);
    }
}
