package com.ssau.btc.sys;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Author: Sergey42
 * Date: 05.04.14 15:36
 */
public class DateUtils {

    protected static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    protected static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    protected static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final int COIN_DESC_HOUR_DIFFERENCE = 4; //todo conf
    public static final String COIN_DESC_TZ = "Etc/GMT+1";

    private static ThreadLocal<Calendar> calendar = new ThreadLocal<Calendar>() {
        @Override
        protected Calendar initialValue() {
            return Calendar.getInstance();
        }
    };

    public static String format(Date date) {
        return dateFormat.format(date);
    }

    public static String formatDateTime(Date date) {
        return dateTimeFormat.format(date);
    }

    public static String formatTime(Date date) {
        return timeFormat.format(date);
    }

    public static String formatSQL(Date date) {
        return "'" + dateFormat.format(date) + "'";
    }

    public static Date getDate(String str) {
        try {
            return dateFormat.parse(str);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date getTime(String str) {
        try {
            return dateTimeFormat.parse(str);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date calcDate(Date from, int unit, int amount) {
        Calendar instance = calendar.get();
        instance.setTime(from);
        instance.add(unit, amount);
        return instance.getTime();
    }
}
