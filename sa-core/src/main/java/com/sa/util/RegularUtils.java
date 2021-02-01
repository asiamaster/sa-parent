package com.sa.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegularUtils {


    private static Pattern BRACE_PATTERN = Pattern.compile ("\\{([^}]*)\\}");


    public static List<String> listBraceParam(String str) {
        ArrayList<String> strings = new ArrayList<>();
        Matcher matcher = BRACE_PATTERN.matcher(str);
        while (matcher.find()) {
            strings.add(matcher.group(1));
        }
        return strings;
    }
}
