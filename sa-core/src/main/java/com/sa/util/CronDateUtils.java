package com.sa.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CronDateUtils {
	private static final String CRON_DATE_FORMAT = "ss mm HH dd MM ? yyyy";


	public static String getCron(final Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(CRON_DATE_FORMAT);
		String formatTimeStr = "";
		if (date != null) {
			formatTimeStr = sdf.format(date);
		}
		return formatTimeStr;
	}


	public static Date getDate(final String cron) throws ParseException {
		if (cron == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(CRON_DATE_FORMAT);
		return sdf.parse(cron);
	}
}
