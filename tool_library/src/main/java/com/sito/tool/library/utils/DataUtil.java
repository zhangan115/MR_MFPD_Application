package com.sito.tool.library.utils;


import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 时间类型转换
 * Created by zhangan on 2017-06-21.
 */

public final class DataUtil {

    /**
     * 格式化时间
     *
     * @param time   时间
     * @param format 格式化
     * @return 结果
     */
    public static String timeFormat(String time, @Nullable String format) {
        if (time == null || time.isEmpty() || time.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        Date d;
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        try {
            d = sdf.parse(time);
        } catch (ParseException e) {
            return sdf.format(new Date(Long.valueOf(time)));
        }
        return sdf.format(d);
    }

    /**
     * 格式化时间
     *
     * @param time   时间
     * @param format 格式化
     * @return 结果
     */
    public static String timeFormat(long time, @Nullable String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        Date d = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(d);
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param date_str 字符串日期
     * @param format   如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static long date2TimeStamp(String date_str, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date_str).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
