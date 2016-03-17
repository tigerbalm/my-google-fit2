package comm.sjun.mygooglefit3.data;

import android.graphics.Color;

import java.util.HashMap;

/**
 * Created by sjun.lee on 2016-03-17.
 */
public class StepsSummary {
    int value;
    String color;
    String highlight;
    String label;

    private static final HashMap<String, String> sColorNameMap;

    static {
        sColorNameMap = new HashMap<String, String>();
        sColorNameMap.put("walking", "#F7464A");
        sColorNameMap.put("running", "#46BFBD");
        sColorNameMap.put("cycling", "#FDB45C");
    }

    public StepsSummary(int value, String label) {
        this.value = value;
        this.label = label;
        this.color = sColorNameMap.get(label);
        this.highlight = "#FF5A5E";
    }
}
