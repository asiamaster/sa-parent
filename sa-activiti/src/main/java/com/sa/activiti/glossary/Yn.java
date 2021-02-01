package com.sa.activiti.glossary;


public enum Yn {
    YES("true","是"),
    NO("false","否");

    private String name;
    private String code ;

    Yn(String code, String name){
        this.code = code;
        this.name = name;
    }

    public static Yn getMenuType(Integer code) {
        for (Yn userState : Yn.values()) {
            if (userState.getCode().equals(code)) {
                return userState;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
