package sys;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
     */
    public void loadCoinDeskIndexes(String startDate, String endDate) {
        String urlPattern = "http://api.coindesk.com/charts/data?output=csv&data=close&startdate=%s&enddate=%s&exchanges=bpi";

        String url = String.format(urlPattern, startDate, endDate);

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);

            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream inputStream = httpEntity.getContent();

            List lines = IOUtils.readLines(inputStream);
            for (Object line : lines) {
                System.out.println(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
