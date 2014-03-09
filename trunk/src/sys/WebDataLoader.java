package sys;

import model.IndexSnapshot;
import model.SnapshotMode;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
public class WebDataLoader {

    /**
     * Loads btc indexes from www.coindesk.com
     *
     * @param startDate start date in yyyy-MM-dd format
     * @param endDate   end date in yyyy-MM-dd
     * @param mode      TRUE for OHLC, FALSE for closing price
     */
    public List<IndexSnapshot> loadCoinDeskIndexes(String startDate, String endDate, SnapshotMode mode) {
        String urlPattern = "http://api.coindesk.com/charts/data?output=csv&data=%s&startdate=%s&enddate=%s&exchanges=bpi";

        String url = String.format(urlPattern, mode == SnapshotMode.OHLC ? "ohlc" : "close", startDate, endDate);

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);

            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream inputStream = httpEntity.getContent();

            List lines = IOUtils.readLines(inputStream);

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            List<IndexSnapshot> indexSnapshots = new ArrayList<>(lines.size());
            for (int i = 1; i < lines.size() - 3; i++) {
                String line = (String) lines.get(i);

                IndexSnapshot indexSnapshot;
                if (mode == SnapshotMode.CLOSING_PRICE) {
                    String dateStr = line.substring(1, 11);
                    String valueStr = line.substring(22);

                    indexSnapshot = new IndexSnapshot(dateFormat.parse(dateStr), Double.valueOf(valueStr));
                } else {
                    String dateStr = line.substring(1, 11);
                    String valuesStr = line.substring(22);
                    String[] ohlc = valuesStr.split(",");

                    indexSnapshot = new IndexSnapshot(dateFormat.parse(dateStr),
                            Double.valueOf(ohlc[0]), Double.valueOf(ohlc[1]), Double.valueOf(ohlc[2]), Double.valueOf(ohlc[3]));
                }
                indexSnapshots.add(indexSnapshot);
            }

            for (IndexSnapshot indexSnapshot : indexSnapshots) {
                System.out.println(indexSnapshot);
            }

            return indexSnapshots;
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }
}
