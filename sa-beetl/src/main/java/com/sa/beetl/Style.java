package com.sa.beetl;

import java.util.HashMap;
import java.util.Map;

public class Style {
    Map<String,String> map = new HashMap<String,String>();
    public Style(String style){
        if(style==null)return ;
        String[] items = style.split(";");
        for(String item:items){
            String[] pair = item.split(":");
            if(pair.length==2){
                map.put(pair[0], pair[1]);
            }else{
                map.put(item, null);
            }
        }
    }

    public boolean isHidden(){
        return map.containsKey("hidden");
    }

    public String getWidth(){
        return map.get("width");
    }

    public boolean isPrimary(){
        return map.containsKey("primary");
    }

    public boolean isScroll(){
        return map.containsKey("scroll");
    }

    public boolean isSecondary(){
        return map.containsKey("secondary");
    }

    public boolean isLink(){
        return map.containsKey("link");
    }

    public boolean isDisable(){
        return map.containsKey("disable");
    }

    public boolean isEnable(){
        return map.containsKey("enable");
    }

    public boolean isTime(){
        return map.containsKey("time");
    }

    public boolean isDate(){
        return map.containsKey("date");
    }
}
