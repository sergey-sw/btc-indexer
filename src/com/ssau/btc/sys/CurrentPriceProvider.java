package com.ssau.btc.sys;

import com.intelli.ray.core.ManagedComponent;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Author: Sergey42
 * Date: 05.04.14 18:21
 */
@ManagedComponent(name = "CurrentPriceProvider")
public class CurrentPriceProvider {

    protected static final String COIN_DESC_URL = "http://api.coindesk.com/v1/bpi/currentprice/eur.json";
    protected static final String LAST_PRICES_URL = "http://api.coindesk.com/v1/bpi/historical/close.json?start=%s&end=%s";

    private static DecimalFormat formatter = new DecimalFormat("0.00");

    public Price getCurrentPrice() {
        Date lastInvokeTs = new Date();
        try {
            HttpGet httpGet = new HttpGet(COIN_DESC_URL);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Bad response from URL : " + COIN_DESC_URL);
            }
            HttpEntity httpEntity = httpResponse.getEntity();

            InputStream inputStream = httpEntity.getContent();

            List lines = IOUtils.readLines(inputStream);
            String responseStr = (String) lines.get(0);

            Price price = new Price();

            JSONObject jsonObject = new JSONObject(responseStr);
            JSONObject bpi = jsonObject.getJSONObject("bpi");
            double usd = bpi.getJSONObject("USD").getDouble("rate_float");
            price.usdDouble = usd;
            price.USD = "$ " + formatter.format(usd);
            double eur = bpi.getJSONObject("EUR").getDouble("rate_float");
            price.EUR = "€ " + formatter.format(eur);
            price.ts = DateUtils.formatTime(lastInvokeTs);
            return price;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Price> getLastPrices(int days) {
        Date now = new Date();
        Date before = DateUtils.calcDate(now, Calendar.DATE , -days - 2);

        String url = String.format(LAST_PRICES_URL, DateUtils.format(before), DateUtils.format(now));

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Bad response from URL : " + COIN_DESC_URL);
            }
            HttpEntity httpEntity = httpResponse.getEntity();

            InputStream inputStream = httpEntity.getContent();

            List lines = IOUtils.readLines(inputStream);
            String responseStr = (String) lines.get(0);

            List<Price> prices = new ArrayList<>();

            JSONObject jsonObject = new JSONObject(responseStr);
            JSONObject bpi = jsonObject.getJSONObject("bpi");
            for (String date : (Set<String>) bpi.keySet()) {
                Price price = new Price();
                price.ts = date;
                price.usdDouble = bpi.getDouble(date);
                price.USD = "$ " + formatter.format(price.usdDouble);
                prices.add(price);
            }
            Collections.sort(prices, new Comparator<Price>() {
                @Override
                public int compare(Price o1, Price o2) {
                    Date p1 = DateUtils.getDate(o1.ts);
                    Date p2 = DateUtils.getDate(o2.ts);
                    return -p1.compareTo(p2);
                }
            });

            for (int i = prices.size() - 2; i > -1; i--) {
                Price price = prices.get(i);
                Price prev = prices.get(i + 1);

                price.calcDiff(prev.usdDouble);
            }

            prices.remove(prices.size() - 1);

            return prices;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static class Price {
        public String USD;
        public double usdDouble;
        public String EUR;
        public String ts;
        public String diff;

        @Override
        public String toString() {
            return "Price{" +
                    "USD='" + USD + '\'' +
                    ", EUR='" + EUR + '\'' +
                    ", ts='" + ts + '\'' +
                    ", diff='" + diff + '\'' +
                    '}';
        }

        public String calcDiff(double prev) {
            double diff = ((usdDouble - prev) / prev) * 100;
            this.diff = (diff > 0 ? "+" : "") + formatter.format(diff) + "%";
            return this.diff;
        }
    }
}
