package comm.sjun.mygooglefit3.util;

import java.util.Calendar;

/**
 * Created by user on 2016-03-15.
 */
public class LocalDate {
    Calendar date;

    public static LocalDate getInstance() {
        return new LocalDate();
    }

    private LocalDate() {
        date = Calendar.getInstance();
        clearTime();
    }

    private void clearTime() {
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
    }

    public long getMillis() {
        return date.getTimeInMillis();
    }

    public LocalDate add(int add) {
        date.add(Calendar.DAY_OF_YEAR, add);
        return this;
    }

    public LocalDate setMillis(long millis) {
        date.setTimeInMillis(millis);
        clearTime();

        return this;
    }
}
