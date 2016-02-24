package comm.sjun.mygooglefit3.data;

import android.os.Parcelable;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;

import java.util.List;
import java.util.concurrent.TimeUnit;

import auto.parcel.AutoParcel;
import auto.parcel.AutoParcel.Builder;
import timber.log.Timber;

/**
 * Created by user on 2016-02-16.
 */
@AutoParcel
abstract class WorkoutRecord implements Parcelable {
    static Builder builder() {
        return new AutoParcel_WorkoutRecord.Builder();
    }

    abstract long start();
    abstract long end();

    abstract int steps();
    abstract double calories();
    abstract double distance();

    public static WorkoutRecord fromDataSet(List<DataSet> dataSet) {
        Builder builder = WorkoutRecord.builder();

        for (DataSet ds : dataSet) {
            for (DataPoint dp : ds.getDataPoints()) {
                builder.start(dp.getStartTime(TimeUnit.MILLISECONDS))
                        .end(dp.getEndTime(TimeUnit.MILLISECONDS));

                for (Field field : dp.getDataType().getFields()) {
                    Timber.i("field: " + field.getName());

                    if (field.equals(Field.FIELD_DISTANCE)) {
                        builder.distance(dp.getValue(Field.FIELD_DISTANCE).asFloat());
                    } else if (field.equals(Field.FIELD_STEPS)) {
                        builder.steps(dp.getValue(Field.FIELD_STEPS).asInt());
                    } else if (field.equals(Field.FIELD_CALORIES)) {
                        builder.calories(dp.getValue(Field.FIELD_CALORIES).asFloat());
                    }
                }
            }
        }

        return builder.build();
    }

    @AutoParcel.Builder
    abstract static class Builder {
        abstract Builder start(long start);
        abstract Builder end(long end);

        abstract Builder steps(int steps);
        abstract Builder calories(double calories);
        abstract Builder distance(double distance);

        abstract WorkoutRecord build();
    }
}
