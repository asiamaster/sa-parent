package com.sa.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class MapUtils {
    public static <T> T get(Map params, String key, Class<T> returnType){
        Object retval = null;
        if (params.containsKey(key)) {
            retval = params.get(key);

            if (returnType.isEnum() && retval instanceof String) {
                retval = Enum.valueOf((Class<? extends Enum>) returnType, (String) retval);

            } else if (returnType.isPrimitive()) {

                if (retval == null) {
                    retval = POJOUtils.getPrimitiveDefault(returnType);

                } else if (!returnType.equals(retval.getClass())) {
                    retval = POJOUtils.getPrimitiveValue(returnType, retval);
                }

            } else if (Date.class.isAssignableFrom(returnType)) {

                if (retval != null) {

                    if(String.class.equals(retval.getClass())){
                        retval = toDate(retval.toString());
                    }
                }
            }
            else if(Integer.class.equals(returnType)){
                retval = retval == null ?   null : new Integer(retval.toString());
            }

            else if(Float.class.equals(returnType)){
                retval = retval == null ?   null :  new Float(retval.toString());
            }

            else if(Double.class.equals(returnType)){
                retval = retval == null ?   null : new Double(retval.toString());
            }

            else if(Boolean.class.equals(returnType)){
                if(retval != null)
                {
                    retval = new Boolean(retval.toString());
                }
            }

        } else if (returnType.isPrimitive()) {
            throw new RuntimeException("不支持基础类型!");
        }
        return (T) retval;
    }


    private static Date toDate(String str) {
        assert (str != null);

        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return dateFormater.parse(str);
        } catch (ParseException e) {
        }
        return null;
    }
}
