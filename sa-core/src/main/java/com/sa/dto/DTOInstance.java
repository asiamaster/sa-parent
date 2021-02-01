package com.sa.dto;

import java.util.HashMap;
import java.util.Map;


public class DTOInstance {
    public static final Map<Class<? extends IDTO>, Class<? extends IDTO>> cache = new HashMap<>(20);
    public static final String SUFFIX = "$Impl";

    public static Boolean useInstance = false;
}
