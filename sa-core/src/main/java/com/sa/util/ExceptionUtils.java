package com.sa.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


public class ExceptionUtils {

	public static String buildMessage(String message, Throwable cause) {
		if (cause != null) {
			StringBuilder buf = new StringBuilder();
			if (message != null) {
				buf.append(message).append("; ");
			}
			buf.append("nested exception is ").append(cause);
			return buf.toString();
		}
		else {
			return message;
		}
	}


	public static String getExceptionString(Throwable e, Integer length) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		e.printStackTrace(ps);
		String msg = os.toString();
		if (length != 0 && msg.length() > length) {
			msg=msg.substring(0, length);
		}
		return msg;
	}

	public static String getExceptionString(Exception e, Integer length) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		e.printStackTrace(ps);
		String msg = os.toString();
		if (length != 0 && msg.length() > length) {
			msg=msg.substring(0, length);
		}
		return msg;
	}
}
