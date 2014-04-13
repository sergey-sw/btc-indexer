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

    String urlPattern = "http://api.coindesk.com/charts/data?output=csv&data=%s&startdate=%s&enddate=%s&exchanges=bpi";

    /**
     * Loads btc indexes from www.coindesk.com
     *
     * @param startDate start date in yyyy-MM-dd format
     * @param endDate   end date in yyyy-MM-dd
     * @param mode      TRUE for OHLC, FALSE for closing price
     */
    @Override
    public Collection<IndexSnapshot> loadCoinDeskIndexes(String startDate, String endDate, SnapshotMode mode, int resolution) {

        String url = String.format(urlPattern, mode == SnapshotMode.OHLC ? "ohlc" : "close", startDate, endDate);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(DateUtils.COIN_DESC_TZ));

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);

            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream inputStream = httpEntity.getContent();

            List lines = IOUtils.readLines(inputStream);

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

    @Override
    public Collection<IndexSnapshot> load24HourIndexes(SnapshotMode mode) {
        String startDate = DateUtils.format(DateUtils.calcDate(new Date(), Calendar.HOUR, -24));
        String endDate = DateUtils.format(new Date());

        String url = String.format(urlPattern, mode == SnapshotMode.OHLC ? "ohlc" : "close", startDate, endDate);
        List<String> lines = loadList(url);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(DateUtils.COIN_DESC_TZ));
        String dateOfEndDay = endDate.substring(8);
        int hourOfEndDay = new Date().getHours() - DateUtils.COIN_DESC_HOUR_DIFFERENCE;

        List<IndexSnapshot> indexSnapshots = new ArrayList<>();

        for (int i = 1; i < lines.size() - 3; i++) {
            String line = lines.get(i);

            String hour = line.substring(12, 14);
            String day = line.substring(9, 11);

            if (!day.equals(dateOfEndDay) && Integer.valueOf(hour) < hourOfEndDay) {
                continue;
            }

            String minute = line.substring(15, 17);
            if (!"00".equals(minute)) {
                continue;
            }

            String dateStr = line.substring(1, 20);
            String valueStr = line.substring(22);

            Date date = DateUtils.getDateTime(dateStr);
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

        return indexSnapshots;

    }

    @Override
    public Collection<IndexSnapshot> loadDayIndexes(int days, Date startDate, SnapshotMode snapshotMode) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DATE, days == 1 ? 2 : days);
        Date endDate = calendar.getTime();
        return loadDayIndexes(startDate, endDate, snapshotMode);
    }

    @Override
    public Collection<IndexSnapshot> loadDayIndexes(Date startDay, Date endDay, SnapshotMode snapshotMode) {
        Calendar calendar = Calendar.getInstance();

        String startDate = DateUtils.format(startDay);
        String endDate = DateUtils.format(endDay);

        String url = String.format(urlPattern, snapshotMode == SnapshotMode.OHLC ? "ohlc" : "close", startDate, endDate);
        List<String> lines = loadList(url);

        List<IndexSnapshot> indexSnapshots = new ArrayList<>();
        for (int i = 1; i < lines.size() - 3; i++) {
            String line = lines.get(i);

            String dateStr = line.substring(1, 20);
            String valueStr = line.substring(22);

            Date snapshotDate = DateUtils.getDateTime(dateStr);
            calendar.setTime(snapshotDate);
            calendar.add(Calendar.HOUR, DateUtils.COIN_DESC_HOUR_DIFFERENCE);

            IndexSnapshot indexSnapshot;
            if (snapshotMode == SnapshotMode.CLOSING_PRICE) {
                indexSnapshot = new IndexSnapshot(calendar.getTime(), Double.valueOf(valueStr));
            } else {
                String[] ohlc = valueStr.split(",");

                indexSnapshot = new IndexSnapshot(calendar.getTime(),
                        Double.valueOf(ohlc[0]), Double.valueOf(ohlc[1]), Double.valueOf(ohlc[2]), Double.valueOf(ohlc[3]));
            }
            indexSnapshots.add(indexSnapshot);
        }

        return indexSnapshots;
    }

    @SuppressWarnings("unchecked")
    protected List<String> loadList(String url) {
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);

            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream inputStream = httpEntity.getContent();

            return IOUtils.readLines(inputStream);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
            return Collections.emptyList();
        }
    }
}
