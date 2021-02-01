package com.sa.dto;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class ReturnTypeHandlerFactory {

    static final Map<Class<?>, Strategy> cache = new HashMap<>();

    static {
        cache.put(Long.class, new LongStrategy());
        cache.put(Integer.class, new IntegerStrategy());
        cache.put(Float.class, new FloatStrategy());
        cache.put(Double.class, new DoubleStrategy());
        cache.put(Date.class, new DateStrategy());
        cache.put(Boolean.class, new BooleanStrategy());
        cache.put(Byte.class, new ByteStrategy());
        cache.put(BigDecimal.class, new BigDecimalStrategy());
        cache.put(Clob.class, new ClobStrategy());
        cache.put(Instant.class, new InstantStrategy());
        cache.put(LocalDateTime.class, new LocalDateTimeStrategy());
        cache.put(LocalDate.class, new LocalDateStrategy());
        cache.put(List.class, new ListStrategy());
        cache.put(String.class, new StringStrategy());
        cache.put(Map.class, new MapStrategy());
        cache.put(IDTO.class, new IDTOStrategy());
    }


    public static Object convertValue(Class<?> type, Object value) {
        Strategy strategy = cache.get(type);
        if(strategy == null || value == null){
            return value;
        }
        if(type.isAssignableFrom(value.getClass())){
            return value;
        }
        try {
            return strategy.convert(value);
        } catch (Exception e) {


            return value;
        }
    }


    public interface Strategy {

        Object convert(Object value);
    }


    private static class StringStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return value.toString();
        }
    }


    private static class MapStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return JSONObject.parseObject(value.toString());
        }
    }


    private static class IDTOStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            if(value instanceof IDTO){
                return (IDTO)value;
            }
            DTO dto = new DTO();
            dto.putAll(JSONObject.parseObject(value.toString()));
            return DTOUtils.proxy(dto, (Class)DTOUtils.getDTOClass(value));
        }
    }


    private static class ListStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return Lists.newArrayList(new Object[]{value});
        }
    }


    private static class LongStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:Long.parseLong(value.toString());
        }
    }


    private static class IntegerStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:Integer.parseInt(value.toString());
        }
    }


    private static class FloatStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:Float.parseFloat(value.toString());
        }
    }


    private static class DoubleStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:Double.parseDouble(value.toString());
        }
    }


    private static class ByteStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:Byte.parseByte(value.toString());
        }
    }


    private static class BooleanStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:Boolean.parseBoolean(value.toString());
        }
    }


    private static class BigDecimalStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:new BigDecimal(value.toString());
        }
    }


    private static class ClobStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:getClobString((java.sql.Clob)value);
        }
    }


    private static class InstantStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            if(StringUtils.isBlank(value.toString())){
                return null;
            }
            if(String.class.equals(value.getClass())){
                String format = "yyyy-MM-dd HH:mm:ss";
                if(((String)value).length() == 10){
                    format = "yyyy-MM-dd";
                }else if(((String)value).length() == 23){
                    format = "yyyy-MM-dd HH:mm:ss.SSS";
                }
                return Instant.from(DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault()).parse((String)value));
            } else if(Long.class.equals(value.getClass())){
                return Instant.ofEpochMilli((Long) value);
            }
            return null;
        }
    }


    private static class LocalDateTimeStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            if(StringUtils.isBlank(value.toString())){
                return null;
            }
            if(String.class.equals(value.getClass())){
                String format = "yyyy-MM-dd HH:mm:ss";
                if(((String)value).length() == 10){
                    format = "yyyy-MM-dd";
                }else if(((String)value).length() == 23){
                    format = "yyyy-MM-dd HH:mm:ss.SSS";
                }
                return LocalDateTime.parse((String) value, DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault()));
            }else if(Long.class.equals(value.getClass())){
                return LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) value), ZoneId.systemDefault());
            }
            return null;
        }
    }


    private static class LocalDateStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            if(StringUtils.isBlank(value.toString())){
                return null;
            }
            if(String.class.equals(value.getClass())){
                String format = "yyyy-MM-dd HH:mm:ss";
                if(((String)value).length() == 10){
                    format = "yyyy-MM-dd";
                }else if(((String)value).length() == 23){
                    format = "yyyy-MM-dd HH:mm:ss.SSS";
                }
                return LocalDate.parse((String) value, DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault()));
            }
            return null;
        }
    }


    private static class DateStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            if(StringUtils.isBlank(value.toString())){
                return null;
            }

            if(String.class.equals(value.getClass())){
                try {
                    return StringUtils.isNumeric(value.toString()) ? new Date(Long.parseLong(value.toString())) : DateUtils.parseDate(value.toString(), "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss.SSS", "E MMM dd yyyy HH:mm:ss");
                } catch (ParseException e) {
                    try {
                        return new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss", Locale.US).parse(value.toString());
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (Long.class.equals(value.getClass())) {
                return new Date((Long)value);
            }
            return null;
        }
    }

    private static String getClobString(java.sql.Clob c) {
        try {
            Reader reader=c.getCharacterStream();
            if (reader == null) {
                return null;
            }
            StringBuffer sb = new StringBuffer();
            char[] charbuf = new char[4096];
            for (int i = reader.read(charbuf); i > 0; i = reader.read(charbuf)) {
                sb.append(charbuf, 0, i);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

}
