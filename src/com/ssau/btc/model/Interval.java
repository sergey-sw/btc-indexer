package com.ssau.btc.model;

import java.util.Date;

/**
 * Author: Sergey42
 * Date: 01.04.14 20:30
 */
public enum Interval {
    MINUTE,
    HOUR,
    DAY,
    WEEK;

    public static Interval fromDates(Date from, Date till) {
        if (from.getDay() == till.getDay()) {
            return MINUTE;
        } else if (from.getMonth() == till.getMonth()) {
            return HOUR;
        } else return DAY;
    }
}
