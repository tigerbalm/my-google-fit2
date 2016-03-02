package comm.sjun.mygooglefit3.data;

import android.os.Parcel;

import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import timber.log.Timber;

/**
 * Created by user on 2016-02-22.
 */
public class WorkoutDB {
    static Collection<WorkoutRecord> recordsCollection = new ArrayList<>(100);

    public static void setData(DataReadResult dataReadResult) {
        recordsCollection.clear();

        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Timber.d("Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();

                Timber.d("Number of returned DataSets of buckets is: " + dataSets.size());
                recordsCollection.add(WorkoutRecord.fromDataSet(dataSets));
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            List<DataSet> dataSets = dataReadResult.getDataSets();

            Timber.d("Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            recordsCollection.add(WorkoutRecord.fromDataSet(dataSets));
        }

        dumpCollections();
    }

    private static void dumpCollections() {
        for (WorkoutRecord record : recordsCollection) {
            Timber.i(record.toString());
        }
    }

    public static Collection<WorkoutRecord> queryData(long start, long end) {
        return recordsCollection;
    }

    public static ActivityType[] getDailyActivities(long timemillis) {
        List<ActivityType> list = new ArrayList<>();

        for (WorkoutRecord record : recordsCollection) {
            Timber.d("%d, %s", timemillis, record.toString());

            if (timemillis >= record.start() && timemillis <= record.end()) {
                list.add(ActivityType.create("walking", record.steps(), "#F7464A", "#FF5A5E"));
            }
        }

        list.add(ActivityType.create("walking", 7353, "#F7464A", "#FF5A5E"));

        Timber.d("list.size: " + list.size());

        return list.toArray(new ActivityType[list.size()]);
    }
}
