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

    public static Date calcDate(Date from, int days) {
        Calendar instance = calendar.get();
        instance.setTime(from);
        instance.add(Calendar.DATE, days);
        return instance.getTime();
    }
}
