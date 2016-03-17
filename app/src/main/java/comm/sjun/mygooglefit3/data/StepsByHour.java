package comm.sjun.mygooglefit3.data;

/**
 * Created by sjun.lee on 2016-03-17.
 */
public class StepsByHour {
    String time;
    int steps;
    double calories;
    double distance;

    // {"time": 23, "steps": 10, "calories": 1,  "distance": 0.02}
    public StepsByHour(String time, int steps, double calories, double distance) {
        this.time = time;
        this.steps = steps;
        this.calories = calories;
        this.distance = distance;
    }

    public StepsByHour() {

    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }
}
