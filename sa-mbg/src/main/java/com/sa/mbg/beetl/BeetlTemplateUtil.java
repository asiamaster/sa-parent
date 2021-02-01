package com.sa.mbg.beetl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;


public class BeetlTemplateUtil {


    public static JSONObject getJsonObject(String remark){
        if(StringUtils.isBlank(remark) || !remark.contains("##")) return null;
        String jsonStr = remark.substring(remark.lastIndexOf("##")+2).trim();
        if(jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
            try {
                return JSONObject.parseObject(jsonStr);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


    public static String getFieldName(String remark){
        if(remark != null && remark.contains("##")){
            return remark.substring(0, remark.indexOf("##"));
        }else{
            return remark;
        }
    }


    public static String getComment(String remark){
        if(remark != null && remark.contains("##")){

            if(remark.indexOf("##") == remark.lastIndexOf("##")){
                return remark.substring(remark.indexOf("##")+2);
            }
            return remark.substring(remark.indexOf("##")+2, remark.lastIndexOf("##"));
        }else{
            return remark;
        }
    }

}
