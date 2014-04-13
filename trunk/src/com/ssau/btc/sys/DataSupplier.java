package com.ssau.btc.sys;

import com.intelli.ray.core.Inject;
import com.intelli.ray.core.ManagedComponent;
import com.ssau.btc.model.IndexSnapshot;
import com.ssau.btc.model.SnapshotMode;

import java.util.Collection;
import java.util.Date;

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
        //todo

        return webLoaderAPI.loadDayIndexes(from, till, snapshotMode);
    }
}
