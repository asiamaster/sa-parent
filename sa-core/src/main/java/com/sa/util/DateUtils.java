package com.sa.util;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class DateUtils {


    public static long getServerTime() {
        return System.currentTimeMillis();
    }


    public static String format(Date date) {
        return format(date, "yyyy-MM-dd HH:mm:ss");
    }


    public static String format(String format) {
        return format(LocalDateTime.now(ZoneId.of("GMT+08:00")), format);
    }


    public static String format(LocalDateTime localDateTime, String format) {
        return DateTimeFormatter.ofPattern(format).format(localDateTime);
    }

    public static String format(Date date, String format) {
        if (date == null) {
            return null;
        } else {
            try {
                SimpleDateFormat e = new SimpleDateFormat(format);
                return e.format(date);
            } catch (Exception var3) {
                throw new RuntimeException("日期格式化转换失败", var3);
            }
        }
    }


    public static String dateFormat(long time) {
        return format(new Date(time), "yyyy-MM-dd HH:mm:ss");
    }


    public static String format(String dateStr, String oldFromat, String newFormat) {
        try {
            if (StringUtils.isBlank(dateStr)) {
                return null;
            } else {
                SimpleDateFormat e = new SimpleDateFormat(oldFromat);
                Date date = e.parse(dateStr);
                return format(date, newFormat);
            }
        } catch (Exception var5) {
            throw new RuntimeException("日期格式化转换失败", var5);
        }
    }


    public static Calendar format(String dateStr, String dateStrFormat) {
        try {
            if (StringUtils.isBlank(dateStr)) {
                return null;
            } else {
                SimpleDateFormat e = new SimpleDateFormat(dateStrFormat);
                Date date = e.parse(dateStr);
                Calendar ca = GregorianCalendar.getInstance();
                ca.setTime(date);
                return ca;
            }
        } catch (Exception var5) {
            throw new RuntimeException("日期格式化转换失败", var5);
        }
    }


    public static Date dateStr2Date(String dateStr, String dateStrFormat) {
        try {
            if (StringUtils.isBlank(dateStr)) {
                return null;
            } else {
                SimpleDateFormat e = new SimpleDateFormat(dateStrFormat);
                return e.parse(dateStr);
            }
        } catch (Exception var3) {
            throw new RuntimeException("日期格式化转换失败", var3);
        }
    }


    public static Date addDays(Date date, int amount) {
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }


    public static Date addHours(Date date, int amount) {
        return add(date, 11, amount);
    }


    public static Date reduceHours(Date date, int amount) {
        if (null == date) {
            return null;
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if (amount < 0) {
                cal.add(11, amount);
            } else {
                cal.add(11, -amount);
            }

            return cal.getTime();
        }
    }


    public static Date addSeconds(Date date, int amount) {
        return add(date, Calendar.SECOND, amount);
    }

    public static Date addMilliSeconds(Date date, int amount) {
        return add(date, Calendar.MILLISECOND, amount);
    }


    public static Date addSeconds(int amount) {
        return add(new Date(), Calendar.SECOND, amount);
    }


    private static Date add(Date date, int field, int amount) {
        if (null == date) {
            return null;
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(field, amount);
            return cal.getTime();
        }
    }


    public static int getDayOfMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }


    public static String formatDate2DateTimeStart(String dateStr) {
        Calendar calendar = format(dateStr, "yyyy-MM-dd");
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return format(calendar.getTime());
    }


    public static String formatDate2DateTimeEnd(String dateStr) {
        Calendar calendar = format(dateStr, "yyyy-MM-dd");
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return format(calendar.getTime());
    }


    public static String formatDate2DateTimeEndSecond(String dateStr) {
        Calendar calendar = format(dateStr, "yyyy-MM-dd");
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return format(calendar.getTime());
    }


    public static Date formatDate2DateTimeStart(Date date) {
        if (date == null) {
            return null;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        }
    }


    public static Date formatDate2DateTimeEnd(Date date) {
        if (date == null) {
            return null;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            return calendar.getTime();
        }
    }


    public static Date formatDate2DateTimeEndSecond(Date date) {
        if (date == null) {
            return null;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            return calendar.getTime();
        }
    }


    public static Date formatDateStr2Date(String date) {
        return dateStr2Date(date, "yyyy-MM-dd HH:mm:ss");
    }


    public static Date getCurrentDate() {
        return new Date();
    }


    public static Date addMinutes(Date nextDate, int amount) {
        return add(new Date(), 12, amount);
    }


    public static boolean equals(Date time1, Date time2) {
        return (time1 != null || time2 == null) && (time1 == null || time2 != null) ? (time1 == null && time2 == null ? true : StringUtils.equals(format(time1), format(time2))) : false;
    }


    public static int differentDays(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);


        if(date1.after(date2)){
            int tmp = year1;
            year1 = year2;
            year2 = tmp;
            tmp = day1;
            day1 = day2;
            day2 = tmp;
        }

        if (year1 != year2){
            int timeDistance = 0;

            for (int i = year1; i < year2; i++) {
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0)
                {
                    timeDistance += 366;
                } else
                {
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2 - day1);
        }
        else {
            return day2 - day1;
        }
    }


    public static int differentDaysByMillisecond(Date date1,Date date2){
        return (int) ((date2.getTime() - date1.getTime()) / (1000*3600*24));
    }


    public static LocalDateTime dateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }



    public static LocalDate dateToLocalDate(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalDate();
    }


    public LocalTime dateToLocalTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalTime();
    }



    public static Date localDateTimeToUdate(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }



    public static Date localDateToUdate(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        return Date.from(instant);
    }


    public static Date localTimeToUdate(LocalTime localTime) {
        LocalDate localDate = LocalDate.now();
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }
}
