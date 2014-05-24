package com.ssau.btc.sys;

import com.ssau.btc.model.IndexSnapshot;

import java.util.Date;
import java.util.List;

/**
 * Author: Sergey42
 * Date: 05.04.14 14:58
 */
public interface DatabaseAPI {

    String NAME = "DatabaseAPI";

    void testSettings() throws Exception;

    List<IndexSnapshot> getDailyIndexes();

    void storeDailyIndexes(List<IndexSnapshot> indexSnapshots);

    void removeDailyIndexes(List<Date> dates);

    String getConfig(String name);

    void writeConfig(String name, String value);
}
