package org.weiboad.ragnar.server.util;

import java.util.Calendar;

public class DateTimeHelper {

    public static String TimeStamp2Date(String timestampString, String formats) {
        Long timestamp = Long.parseLong(timestampString) * 1000;
        String date = new java.text.SimpleDateFormat(formats).format(new java.util.Date(timestamp));
        return date;
    }

    public static long getBeforeDay(int day) {
        return (System.currentTimeMillis() / 1000) - day * 24 * 3600;
    }

    //获取今天0点 timestamp
    public static long getTimesMorning(Long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp * 1000);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis() / 1000);
    }

    //获取小时整数 timestamp
    public static Integer getHourTime(Long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp * 1000);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis() / 1000);
    }

    //获取当前时间
    public static long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

}
