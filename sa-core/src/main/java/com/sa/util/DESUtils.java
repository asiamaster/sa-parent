package com.sa.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;


public class DESUtils {

    public static final String KEY_ALGORITHM = "DES";


    public static String encrypt(String srcStr, Charset charset, String sKey) {
        if(charset == null){
            charset = Charset.forName("UTF-8");
        }
        byte[] src = srcStr.getBytes(charset);
        byte[] buf = encrypt(src, sKey);
        return parseByte2HexStr(buf);
    }


    public static String decrypt(String hexStr, Charset charset, String sKey) throws Exception {
        if(charset == null){
            charset = Charset.forName("UTF-8");
        }
        byte[] src = parseHexStr2Byte(hexStr);
        byte[] buf = decrypt(src, sKey);
        return new String(buf, charset);
    }


    private static byte[] encrypt(byte[] data, String password) {
        try{
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password.getBytes());

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            SecretKey securekey = keyFactory.generateSecret(desKey);

            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);


            return cipher.doFinal(data);
        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;
    }


    private static byte[] decrypt(byte[] data, String password) throws Exception {

        SecureRandom random = new SecureRandom();

        DESKeySpec desKey = new DESKeySpec(password.getBytes());

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);

        SecretKey securekey = keyFactory.generateSecret(desKey);

        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);

        cipher.init(Cipher.DECRYPT_MODE, securekey, random);

        return cipher.doFinal(data);
    }


    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }


    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }


    private final static String charset = "UTF-8";
    private final static String algorithm = "DES";

    public static String decrypt(String encryptStr, String key) {
        String strDecrypt = null;
        try {
            strDecrypt = new String(decryptByte(Base64.getDecoder().decode(encryptStr), key), charset);
        } catch (Exception e) {
            throw new RuntimeException("decrypt exception", e);
        }
        return strDecrypt;
    }


    public static byte[] decryptByte(byte[] encryptByte, String key) {
        Cipher cipher;
        byte[] decryptByte = null;
        try {
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, generateKey(key));
            decryptByte = cipher.doFinal(encryptByte);
        } catch (Exception e) {
            throw new RuntimeException("decryptByte exception", e);
        } finally {
            cipher = null;
        }
        return decryptByte;
    }


    public static Key generateKey(String strKey) {
        try {
            DESKeySpec desKeySpec = new DESKeySpec(strKey.getBytes(charset));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
            return keyFactory.generateSecret(desKeySpec);
        } catch (Exception e) {
            throw new RuntimeException("generateKey exception", e);
        }
    }


    public static byte[] encryptByte(byte[] srcByte, String key) {
        byte[] byteFina = null;
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(key));
            byteFina = cipher.doFinal(srcByte);
        } catch (Exception e) {
            throw new RuntimeException("字节加密异常",e);
        } finally {
            cipher = null;
        }
        return byteFina;
    }



    public static String encrypt(String srcStr, String key) {
        String strEncrypt = null;
        try {
            strEncrypt = Base64.getEncoder().encodeToString(encryptByte(srcStr.getBytes(charset), key));
        } catch (Exception e) {
            throw new RuntimeException("加密异常", e);
        }
        return strEncrypt;
    }
}
