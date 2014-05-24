package com.ssau.btc.sys;

import com.intelli.ray.core.Inject;
import com.intelli.ray.core.ManagedComponent;
import com.ssau.btc.model.IndexSnapshot;
import com.ssau.btc.model.Interval;
import com.ssau.btc.model.SnapshotMode;
import com.ssau.btc.utils.DateUtils;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: Sergey42
 * Date: 13.04.14 13:53
 */
@ManagedComponent(name = "DataSupplier")
public class DataSupplier {

    @Inject
    protected WebLoaderAPI webLoaderAPI;

    @Inject
    protected DatabaseAPI databaseAPI;

    public Collection<IndexSnapshot> getIndexSnapshots(Date from, Date till, SnapshotMode snapshotMode) {
        return getIndexSnapshots(from, till, snapshotMode, Interval.fromDates(from, till));
    }

    public Collection<IndexSnapshot> getIndexSnapshots(Date from, Date till, SnapshotMode snapshotMode, Interval interval) {
        Collection<IndexSnapshot> indexSnapshots = webLoaderAPI.loadDayIndexes(from, till, snapshotMode);
        Map<String, IndexSnapshot> filteredSnapshots = new LinkedHashMap<>();
        for (IndexSnapshot indexSnapshot : indexSnapshots) {
            String date = "";
            if (interval == Interval.DAY) {
                date = DateUtils.format(indexSnapshot.date);
            } else if (interval == Interval.HOUR) {
                date = DateUtils.formatHour(indexSnapshot.date);
            } else if (interval == Interval.MINUTE) {
                date = DateUtils.formatMinutes(indexSnapshot.date);
            }

            if (!filteredSnapshots.containsKey(date)) {
                filteredSnapshots.put(date, indexSnapshot);
            }
        }
        return filteredSnapshots.values();
    }
}
