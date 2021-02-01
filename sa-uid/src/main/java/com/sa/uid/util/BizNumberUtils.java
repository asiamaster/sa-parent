package com.sa.uid.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;


public class BizNumberUtils {

    
    public static Long getInitBizNumber(String dateStr, int length) {
        return StringUtils.isBlank(dateStr) ? 1 : NumberUtils.toLong(dateStr) * new Double(Math.pow(10, length)).longValue() + 1;
    }
}
