package com.ssau.btc.model;

import java.util.*;

/**
 * Author: Sergey42
 * Date: 05.03.14 22:27
 */
public class IndexSnapshotUtils {

    public static final int OPEN = 0;
    public static final int HIGH = 1;
    public static final int LOW = 2;
    public static final int CLOSE = 3;

    public static double[] parseClosingPrice(Collection<IndexSnapshot> indexSnapshots) {
        double[] values = new double[indexSnapshots.size()];

        int k = 0;
        for (IndexSnapshot indexSnapshot : indexSnapshots) {
            values[k++] = indexSnapshot.value;// indexSnapshots.get(i).value;
        }

        return values;
    }

    public static double[] parseOHLC(List<IndexSnapshot> indexSnapshots, int mode) {
        double[] values = new double[indexSnapshots.size()];

        for (int i = 0; i < indexSnapshots.size(); i++) {
            switch (mode) {
                case OPEN:
                    values[i] = indexSnapshots.get(i).open;
                    break;
                case HIGH:
                    values[i] = indexSnapshots.get(i).high;
                    break;
                case CLOSE:
                    values[i] = indexSnapshots.get(i).close;
                    break;
                case LOW:
                    values[i] = indexSnapshots.get(i).low;
                    break;
            }
        }

        return values;
    }

    public static List<IndexSnapshot> convertToSnapshots(double[] data, Date startDate, Interval interval) {
        List<IndexSnapshot> indexSnapshots = new ArrayList<>(data.length);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        for (double value : data) {

            switch (interval) {
                case DAY:
                    calendar.add(Calendar.DATE, 1);
                    break;
                case HOUR:
                    calendar.add(Calendar.HOUR, 1);
                    break;
            }

            IndexSnapshot indexSnapshot = new IndexSnapshot(calendar.getTime(), value);
            indexSnapshots.add(indexSnapshot);
        }

        return indexSnapshots;
    }
}
