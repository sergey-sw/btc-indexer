package com.ssau.btc.sys;

import com.ssau.btc.model.IndexSnapshot;
import com.ssau.btc.model.SnapshotMode;

import java.util.Collection;

/**
 * Author: Sergey42
 * Date: 05.04.14 16:07
 */
public interface WebLoaderAPI {

    String NAME = "WebDataLoader";

    Collection<IndexSnapshot> loadCoinDeskIndexes(String startDate, String endDate, SnapshotMode mode);
}
