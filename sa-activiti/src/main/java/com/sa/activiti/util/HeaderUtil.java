package com.sa.activiti.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

public final class HeaderUtil {


    private static final Logger log = LoggerFactory.getLogger(HeaderUtil.class);


    private static final String APPLICATION_NAME = "****";


    private HeaderUtil() {
    }


    public static HttpHeaders createAlert(String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-CAPGENIE-ALERT", message);
        headers.add("X-CAPGENIE-PARAMS", param);
        return headers;
    }


    public static HttpHeaders createEntityCreationAlert(String entityName, String param) {
        return createAlert(APPLICATION_NAME + "." + entityName + ".created", param);
    }


    public static HttpHeaders createEntityUpdateAlert(String entityName, String param) {
        return createAlert(APPLICATION_NAME + "." + entityName + ".updated", param);
    }


    public static HttpHeaders createEntityDeletionAlert(String entityName, String param) {
        return createAlert(APPLICATION_NAME + "." + entityName + ".deleted", param);
    }


    public static HttpHeaders createFailureAlert(String entityName, String errorKey, String defaultMessage) {
        log.error("Entity processing failed, {}", defaultMessage);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-CAPGENIE-ERROR", "exception." + errorKey);
        headers.add("X-CAPGENIE-PARAMS", entityName);
        return headers;
    }
}