package com.ssau.btc.sys;

import com.intelli.ray.core.ManagedComponent;
import com.ssau.btc.model.IndexSnapshot;
import com.ssau.btc.model.SnapshotMode;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
@ManagedComponent(name = WebLoaderAPI.NAME)
public class WebDataLoader implements WebLoaderAPI {

    /**
     * Loads btc indexes from www.coindesk.com
     *
     * @param startDate start date in yyyy-MM-dd format
     * @param endDate   end date in yyyy-MM-dd
     * @param mode      TRUE for OHLC, FALSE for closing price
     */
    @Override
    public Collection<IndexSnapshot> loadCoinDeskIndexes(String startDate, String endDate, SnapshotMode mode, int resolution) {
        String urlPattern = "http://api.coindesk.com/charts/data?output=csv&data=%s&startdate=%s&enddate=%s&exchanges=bpi";

        String url = String.format(urlPattern, mode == SnapshotMode.OHLC ? "ohlc" : "close", startDate, endDate);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(DateUtils.COIN_DESC_TZ));

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);

            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream inputStream = httpEntity.getContent();

            List lines = IOUtils.readLines(inputStream);

            if (!"Date,\"Close Price\"".equals(lines.get(0))) {
                System.err.println("Check web load URL");
                return new ArrayList<>();
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateOfEndDay = endDate.substring(8);
            int hourOfEndDay = new Date().getHours() - DateUtils.COIN_DESC_HOUR_DIFFERENCE;

            List<IndexSnapshot> indexSnapshots = new ArrayList<>();

            for (int i = 1; i < lines.size() - 3; i++) {
                String line = (String) lines.get(i);

                if (resolution == HOUR) {
                    String hour = line.substring(12, 14);
                    String day = line.substring(9, 11);

                    if (!day.equals(dateOfEndDay) && Integer.valueOf(hour) < hourOfEndDay) {
                        continue;
                    }

                    String minute = line.substring(15, 17);
                    if (!"00".equals(minute)) {
                        continue;
                    }
                }

                String dateStr = line.substring(1, 20);
                String valueStr = line.substring(22);

                Date date = dateFormat.parse(dateStr);
                calendar.setTime(date);
                calendar.add(Calendar.HOUR, DateUtils.COIN_DESC_HOUR_DIFFERENCE);

                IndexSnapshot indexSnapshot;
                if (mode == SnapshotMode.CLOSING_PRICE) {
                    indexSnapshot = new IndexSnapshot(calendar.getTime(), Double.valueOf(valueStr));
                } else {
                    String[] ohlc = valueStr.split(",");

                    indexSnapshot = new IndexSnapshot(calendar.getTime(),
                            Double.valueOf(ohlc[0]), Double.valueOf(ohlc[1]), Double.valueOf(ohlc[2]), Double.valueOf(ohlc[3]));
                }
                indexSnapshots.add(indexSnapshot);
            }

            if (resolution == DAY && DateUtils.format(new Date()).equals(endDate)) {
                indexSnapshots.remove(indexSnapshots.size() - 1);
            }

            return indexSnapshots;
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }
}
