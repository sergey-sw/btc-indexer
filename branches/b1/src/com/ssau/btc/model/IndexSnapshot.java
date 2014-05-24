package com.ssau.btc.model;

import com.ssau.btc.utils.DateUtils;

import java.util.Date;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
public final class IndexSnapshot {

    public SnapshotMode mode;

    public Date date;
    public double value;

    public double open;
    public double high;
    public double low;
    public double close;

    public IndexSnapshot(Date date, double value) {
        mode = SnapshotMode.CLOSING_PRICE;
        this.date = date;
        this.value = value;
    }

    public IndexSnapshot(Date date, double open, double high, double low, double close) {
        mode = SnapshotMode.OHLC;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    @Override
    public String toString() {
        if (mode == SnapshotMode.CLOSING_PRICE)
            return "IndexSnapshot {" +
                    "date=" + DateUtils.format(date) +
                    ", value=" + value +
                    '}';
        else
            return "IndexSnapshot {" +
                    "date=" + DateUtils.format(date) +
                    ", values=[" + open + "," + high + "," + low + "," + close + "]}";
    }
}
