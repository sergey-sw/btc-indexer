package model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
public final class IndexSnapshot {

    public static final Boolean CLOSING_PRICE = false;
    public static final Boolean OHLC = true;

    public boolean mode;

    public Date date;
    public double value;

    public double open;
    public double high;
    public double low;
    public double close;

    protected static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public IndexSnapshot(Date date, double value) {
        mode = CLOSING_PRICE;
        this.date = date;
        this.value = value;
    }

    public IndexSnapshot(Date date, double open, double high, double low, double close) {
        mode = OHLC;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    @Override
    public String toString() {
        if (!mode)
            return "IndexSnapshot {" +
                    "date=" + dateFormat.format(date) +
                    ", value=" + value +
                    '}';
        else
            return "IndexSnapshot {" +
                    "date=" + dateFormat.format(date) +
                    ", values=[" + open + "," + high + "," + low + "," + close + "]}";
    }
}
