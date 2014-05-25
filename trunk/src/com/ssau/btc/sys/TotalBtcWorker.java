package com.ssau.btc.sys;

import com.intelli.ray.core.Inject;
import com.intelli.ray.core.ManagedComponent;
import com.ssau.btc.utils.DateUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Sergey42
 * Date: 25.05.14 13:05
 */
@ManagedComponent(name = TotalBtcWorker.NAME)
public class TotalBtcWorker {

    @Inject
    protected DatabaseAPI databaseAPI;
    @Inject
    protected WebLoaderAPI webLoaderAPI;

    public static final String NAME = "TotalBtcWorker";
    public static final String LOCAL_PATH = "com/ssau/btc/resources/total-btc.txt";
    protected final String BLOCK_CHAIN_URL = "http://blockchain.info/charts/total-bitcoins?timespan=%s&format=csv";

    @SuppressWarnings("unchecked")
    public void init() {
        String config = databaseAPI.getConfig(ConfigKeys.BTC_WORKER_INIT);
        if (!Boolean.valueOf(config)) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(LOCAL_PATH);
            try {
                List<String> lines = IOUtils.readLines(stream);
                Map<Date, Integer> map = new LinkedHashMap<>(lines.size());
                for (String line : lines) {
                    String dateStr = line.substring(0, 10);
                    Date date = DateUtils.getTotalBtcDate(dateStr);
                    String valueStr = line.substring(20);
                    Integer value = Double.valueOf(valueStr).intValue();
                    map.put(date, value);
                }

                if (databaseAPI.storeTotalBtc(map)) {
                    databaseAPI.writeConfig(ConfigKeys.BTC_WORKER_INIT, "true");
                }
            } catch (IOException e) {
                ExceptionHandler.handle(e);
            }
        } else {
            Date lastDate = databaseAPI.getLastDateInTotalBtc();
            Date now = new Date();

            int dayDiffer = DateUtils.calcApproxDayDifference(lastDate, now);
            if (dayDiffer == 0) {
                return;
            }

            String dateParam = "";
            if (dayDiffer > 1 && dayDiffer < 30) {
                dateParam = "30days";
            } else if (dayDiffer >= 30 && dayDiffer < 60) {
                dateParam = "60days";
            } else if (dayDiffer >= 60 && dayDiffer < 180) {
                dateParam = "180days";
            } else {
                throw new RuntimeException("Contact the developer");
            }

            String url = String.format(BLOCK_CHAIN_URL, dateParam);
            List<String> lines = webLoaderAPI.loadList(url);

            for (String line : lines) {
                String dateStr = line.substring(0, 10);
                Date date = DateUtils.getTotalBtcDate(dateStr);
                String valueStr = line.substring(20);
                Integer value = Double.valueOf(valueStr).intValue();
                databaseAPI.storeSingleTotalBtc(date, value);
            }
        }
    }

    public double[] getByPeriod(Date date1, Date date2) {
        return databaseAPI.loadTotalBtcByPeriod(date1, date2);
    }
}
