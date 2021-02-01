package com.sa.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AESUtils {

    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";


    public static String encrypt(String content, String password) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password));
            byte[] result = cipher.doFinal(byteContent);
            return Base64.encodeBase64String(result);
        } catch (Exception ex) {
            Logger.getLogger(AESUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    public static String decrypt(String content, String password) {
        try {

            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password));

            byte[] result = cipher.doFinal(Base64.decodeBase64(content));
            return new String(result, "utf-8");
        } catch (Exception ex) {
            Logger.getLogger(AESUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    private static SecretKeySpec getSecretKey(final String password) {

        KeyGenerator kg = null;
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(password.getBytes());
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);

            kg.init(128, secureRandom);

            SecretKey secretKey = kg.generateKey();
            return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AESUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void main1(String[] args) {
        String s = "{\n" +
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
        System.out.println("s:" + s);
        Long start = System.currentTimeMillis();
        String s1 = AESUtils.encrypt(s, "12345678");
        System.out.println("s1:" + s1);
        System.out.println("aes encrypt cost:"+ (System.currentTimeMillis()-start)+"ms");
        start = System.currentTimeMillis();
        System.out.println("s2:"+ AESUtils.decrypt(s1, "12345678"));
        System.out.println("aes decrypt cost:"+ (System.currentTimeMillis()-start)+"ms");


    }

}