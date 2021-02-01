package com.sa.mvc.controller;

import com.sa.metadata.ValuePair;
import com.sa.metadata.ValueProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/provider")
public class ProviderController {
    private static final String PROVIDER_KEY = "provider";

    @Autowired
    private ValueProviderUtils valueProviderUtils;


    @RequestMapping("/getLookupList.action")
    public @ResponseBody
    List<ValuePair<?>> getLookupList(HttpServletRequest request, HttpServletResponse response, @RequestBody String queryParams) throws UnsupportedEncodingException {
        queryParams=java.net.URLDecoder.decode(queryParams,"UTF-8");
        Map<String, Object> queryMap = parseQuery(queryParams);
        String provider = queryMap.get(PROVIDER_KEY).toString();
        queryMap.remove(PROVIDER_KEY);

        return valueProviderUtils.getLookupList(provider, queryMap.get("value"), queryMap);
    }


    private Map<String, Object> parseQuery(String queryParams) {
        String[] params = queryParams.split("&");
        Map<String, Object> map = new LinkedHashMap<String, Object>(params.length);
        for (String param : params) {
            String[] strings = param.split("=");
            String name = strings[0];
            String value = null;
            if (strings.length > 1) {
                value = strings[1];
            }
            map.put(name, value);
        }
        return map;
    }




}
