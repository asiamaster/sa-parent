package com.sa.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class EncipherService {

    @Value("${privateKey:}")
    private String privateKey = "MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAjpZqmZBlsYuCJctl+XZfW9h6mTa7FDegFF7TXxsoc7OYNELZFy+mZMyBqaIa0KPlc7yyXnci/bcEaojtilMdQwIDAQABAkARHAt+kc0iGNNtJZL+9C1NIBTV/bNFIcebqiVC7EdWWrB5IgZcWXbsdlDxY5rb/IkesDXp3EfBNOs8djvyYsUBAiEA06yTVF6lB7e08ctqP3miHX1iFwkgcqupJNxefK4t+TMCIQCscj/Pxt7kRxgq1PSHPDPgmRPhlM+8Hx5lXrhcRBjrsQIgQC7z/YE1SUHK/AZSES0wmwCJ3bJGxH6Iq0Sm4eduyUECIG5zXtu+DNU5lAkbDOPxc2jPoyRBJCzh4Z4b5QlztGKBAiBbL1TmCdYYipJeCADGUyXzoojZ1+iMO0CQzI6jrj/Vlg==";

    @Value("${publicKey:}")
    private String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAI6WapmQZbGLgiXLZfl2X1vYepk2uxQ3oBRe018bKHOzmDRC2RcvpmTMgamiGtCj5XO8sl53Iv23BGqI7YpTHUMCAwEAAQ==";

    public static final String KEY = "key";
    public static final String DATA = "data";


    public String encryptByPublicKey(String data, String key) throws Exception {
        String desKey = RandomString.createRandomString(32);
        String encodeData = DESUtils.encrypt(data, null, desKey);
        byte[] encryptDesKey = RSAUtils.encryptByPublicKey(desKey.getBytes(), Base64.decodeBase64(publicKey));
        desKey = DESUtils.parseByte2HexStr(encryptDesKey);
        JSONObject jo = new JSONObject();
        jo.put(KEY, desKey);
        jo.put(DATA, encodeData);
        return jo.toJSONString();
    }


    public String decryptByPrivateKey(String json, String key) throws Exception {
        JSONObject jo = JSON.parseObject(json);
        String encodedDesKey = jo.getString(KEY);
        byte[] encodedDesKeyBytes = DESUtils.parseHexStr2Byte(encodedDesKey);
        String encodedData = jo.getString(DATA);
        String desKey = new String(RSAUtils.decryptByPrivateKey(encodedDesKeyBytes, Base64.decodeBase64(privateKey)));
        return DESUtils.decrypt(encodedData, null, desKey);
    }

    public static void main1(String[] args) throws Exception {
        String json = "{\n" +
                "    \"type\": \"json\",\n" +
                "    \"from\": \"settlement\",\n" +
                "    \"sendTime\": \"2018-09-07 15:02:03\",\n" +
                "    \"data\": {\n" +
                "        \"certificateType\": \"id\",\n" +
                "        \"certificateNumber\": \"510100201202021111\",\n" +
                "        \"sourceSystem\": \"settlement\",\n" +
                "        \"market\" : \"hd\", \n" +
                "        \"type\" : \"purchase\", \n" +
                "        \"organizationType\": \"individuals\",\n" +
                "        \"name\": \"test090701\",\n" +
                "        \"sex\": \"male\",\n" +
                "        \"phone\": \"13000000001\",\n" +
                "        \"created\": \"2018-03-25 17:25:36\",\n" +
                "        \"address\": [\n" +
                "            \"四川省成都市锦江区人民东路6号\",\n" +
                "            \"成都市人民北路8号\"\n" +
                "        ],\n" +
                "        \"extensions\": [\n" +
                "            {\n" +
                "                \"acctId\": \"1001\",\n" +
                "                \"notes\": \"卡号:10315265\",\n" +
                "                \"acctType\": \"masterCard\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"acctId\": \"1001\",\n" +
                "                \"notes\": \"卡号:10315265\",\n" +
                "                \"acctType\": \"masterCard\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        String data1 = DESUtils.encrypt(json, null, "12345678");
        System.out.println(DESUtils.decrypt(data1, null, "12345678"));


        EncipherService encipherService = new EncipherService();
        String data = encipherService.encryptByPublicKey(json, "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAI6WapmQZbGLgiXLZfl2X1vYepk2uxQ3oBRe018bKHOzmDRC2RcvpmTMgamiGtCj5XO8sl53Iv23BGqI7YpTHUMCAwEAAQ==");
        System.out.println("1111:"+data);
        System.out.println();
        data = encipherService.decryptByPrivateKey(data, "MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAjpZqmZBlsYuCJctl+XZfW9h6mTa7FDegFF7TXxsoc7OYNELZFy+mZMyBqaIa0KPlc7yyXnci/bcEaojtilMdQwIDAQABAkARHAt+kc0iGNNtJZL+9C1NIBTV/bNFIcebqiVC7EdWWrB5IgZcWXbsdlDxY5rb/IkesDXp3EfBNOs8djvyYsUBAiEA06yTVF6lB7e08ctqP3miHX1iFwkgcqupJNxefK4t+TMCIQCscj/Pxt7kRxgq1PSHPDPgmRPhlM+8Hx5lXrhcRBjrsQIgQC7z/YE1SUHK/AZSES0wmwCJ3bJGxH6Iq0Sm4eduyUECIG5zXtu+DNU5lAkbDOPxc2jPoyRBJCzh4Z4b5QlztGKBAiBbL1TmCdYYipJeCADGUyXzoojZ1+iMO0CQzI6jrj/Vlg==");
        System.out.println("2222:"+data);
    }
}
