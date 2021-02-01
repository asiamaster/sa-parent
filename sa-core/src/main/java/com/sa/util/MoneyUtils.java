package com.sa.util;

import java.math.BigDecimal;


public class MoneyUtils {
    public static final String YUAN = "元";
    public static final String JIAO = "角";
    public static final String CENT = "分";

    public MoneyUtils() {
    }

    public static String centToYuan(int cent) {
        int yuan = cent / 100;
        int mod = Math.abs(cent % 100);
        int jiao = mod / 10;
        int fen = mod % 10;
        if(cent < 0 && cent >-100){
            return "-" + yuan + "." + jiao + "" + fen;
        }
        return yuan + "." + jiao + "" + fen;
    }

    public static String centToYuan(Long cent) {
        if(cent != null){
            long yuan = cent.longValue() / 100L;
            long mod = Math.abs(cent.longValue() % 100L);
            long jiao = mod / 10L;
            long fen = mod % 10L;
            if(cent < 0 && cent >-100){
                return "-" + yuan + "." + jiao + "" + fen;
            }
            return yuan + "." + jiao + "" + fen;
        } else {
            return "0.00";
        }
    }

    public static long yuanToCent(Double v) {
        BigDecimal bd = BigDecimal.valueOf(v.doubleValue());
        BigDecimal h = new BigDecimal(100);
        BigDecimal b = bd.multiply(h);
        return b.longValue();
    }

    public static long yuanToCent(String v) {
        if(v != null && v.length() > 0) {
            try {
                Double e = Double.valueOf(Double.parseDouble(v));
                return yuanToCent((Double)e);
            } catch (Exception var2) {
                return 0L;
            }
        } else {
            return 0L;
        }
    }

    public static void main1(String[] args) {
        String yuan = centToYuan(100000);
        System.out.println(yuan + "元");
        String jiao = centToJiao(100000);
        System.out.println(jiao + "角");
    }

    public static String centToJiao(int cent) {
        if(cent < 1) {
            return "0.0";
        } else {
            int jiao = cent / 10;
            int fen = cent % 10;
            return jiao + "." + fen;
        }
    }

    public static String centToJiao(Long cent) {
        return cent != null && cent.longValue() > 0L?centToJiao(Integer.parseInt(cent + "")):"0.0";
    }
}
