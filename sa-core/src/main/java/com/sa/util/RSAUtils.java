package com.sa.util;


import javax.crypto.Cipher;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


public class RSAUtils {

    public static final String KEY_ALGORITHM = "RSA";

    private static RSAKeyPair rsaKeyPair;

    private static final int KEY_SIZE = 1024;

    private static final String PUBLIC_KEY = "RSAPublicKey";

    private static final String PRIVATE_KEY = "RSAPrivateKey";


    public static RSAKeyPair getRSAKeyPair() throws NoSuchAlgorithmException {
        if(rsaKeyPair == null){
            synchronized (RSAKeyPair.class){
                if(rsaKeyPair == null) {
                    rsaKeyPair = new RSAKeyPair();
                    Map<String, Object> map = createKey();
                    rsaKeyPair.setPrivateKey((RSAPrivateKey) map.get(PRIVATE_KEY));
                    rsaKeyPair.setPublicKey((RSAPublicKey) map.get(PUBLIC_KEY));
                }
            }
        }
        return rsaKeyPair;
    }


    public static RSAKeyPair getRSAKeyPair(String publicKey, String privateKey) throws Exception {
        if(rsaKeyPair == null){
            synchronized (RSAKeyPair.class){
                if(rsaKeyPair == null) {
                    rsaKeyPair = new RSAKeyPair();
                    rsaKeyPair.setPublicKey(getPublicKey(publicKey));
                    rsaKeyPair.setPrivateKey(getPrivateKey(privateKey));
                }
            }
        }
        return rsaKeyPair;
    }


    public static synchronized RSAKeyPair createRSAKeyPair(String publicKey, String privateKey) throws Exception {
        RSAKeyPair rsaKeyPair = new RSAKeyPair();
        rsaKeyPair.setPublicKey(getPublicKey(publicKey));
        rsaKeyPair.setPrivateKey(getPrivateKey(privateKey));
        return rsaKeyPair;
    }


    public static Map<String, Object> createKey() throws NoSuchAlgorithmException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);

        keyPairGenerator.initialize(KEY_SIZE);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        Map<String, Object> keyMap = new HashMap<String, Object>();
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }


    public static Map<String, Object> createKey(int KEY_SIZE) throws Exception {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);

        keyPairGenerator.initialize(KEY_SIZE);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        Map<String, Object> keyMap = new HashMap<String, Object>();
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }



    public static byte[] getPublicKeyBytes(Map<String, Object> keyMap) throws Exception {

        Key key = (Key) keyMap.get(PUBLIC_KEY);

        return key.getEncoded();
    }


    public static byte[] getPrivateKeyBytes(Map<String, Object> keyMap) throws Exception {

        Key key = (Key) keyMap.get(PRIVATE_KEY);

        return key.getEncoded();
    }


    public static String getPublicKey(Map<String, Object> keyMap) throws Exception {

        Key key = (Key) keyMap.get(PUBLIC_KEY);

        return encryptBASE64(key.getEncoded());
    }


    public static String getPublicKey(RSAKeyPair rsaKeyPair) {

        Key key = rsaKeyPair.getPublicKey();

        return encryptBASE64(key.getEncoded());
    }


    public static String getPrivateKey(Map<String, Object> keyMap) {

        Key key = (Key) keyMap.get(PRIVATE_KEY);

        return encryptBASE64(key.getEncoded());
    }


    public static RSAPublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        RSAPublicKey publicKey =  (RSAPublicKey)keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    public static RSAPrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        RSAPrivateKey privateKey = (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
        return privateKey;
    }


    public static String getPrivateKey(RSAKeyPair rsaKeyPair) {

        Key key = rsaKeyPair.getPrivateKey();

        return encryptBASE64(key.getEncoded());
    }


    public static byte[] decryptBASE64(String key) throws IOException {
        return Base64.getDecoder().decode(key);
    }


    public static String encryptBASE64(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }


    public static byte[] encryptByPrivateKey(byte[] data, byte[] key) throws Exception {

        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }


    public static byte[] encryptByPublicKey(byte[] data, byte[] key) throws Exception {

        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);


        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);

        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);

        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return cipher.doFinal(data);
    }


    public static byte[] decryptByPrivateKey(byte[] data, byte[] key) throws Exception {

        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }


    public static byte[] decryptByPublicKey(byte[] data, byte[] key) throws Exception {

        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);


        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);

        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);

        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        return cipher.doFinal(data);
    }



    public static String getRSAPrivateKeyAsNetFormat(byte[] encodedPrivkey) {
        try {
            StringBuffer buff = new StringBuffer(2048);
            PKCS8EncodedKeySpec pvkKeySpec = new PKCS8EncodedKeySpec(encodedPrivkey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            RSAPrivateCrtKey pvkKey = (RSAPrivateCrtKey) keyFactory.generatePrivate(pvkKeySpec);
            buff.append("<RSAKeyValue>");
            buff.append("<Modulus>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getModulus().toByteArray())) + "</Modulus>");
            buff.append("<Exponent>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getPublicExponent().toByteArray())) + "</Exponent>");
            buff.append("<P>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getPrimeP().toByteArray())) + "</P>");
            buff.append("<Q>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getPrimeQ().toByteArray())) + "</Q>");
            buff.append("<DP>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getPrimeExponentP().toByteArray())) + "</DP>");
            buff.append("<DQ>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getPrimeExponentQ().toByteArray())) + "</DQ>");
            buff.append("<InverseQ>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getCrtCoefficient().toByteArray())) + "</InverseQ>");
            buff.append("<D>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getPrivateExponent().toByteArray())) + "</D>");
            buff.append("</RSAKeyValue>");
            return buff.toString().replaceAll("[ \t\n\r]", "");
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }


    public static String getRSAPublicKeyAsNetFormat(byte[] encodedPrivkey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(encodedPrivkey));
            return getRSAPublicKeyAsNetFormat(pubKey);
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }


    public static String getRSAPublicKeyAsNetFormat(RSAPublicKey pubKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("<RSAKeyValue>");
        sb.append("<Modulus>").append(org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pubKey.getModulus().toByteArray()))).append("</Modulus>");
        sb.append("<Exponent>").append(org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pubKey.getPublicExponent().toByteArray()))).append("</Exponent>");
        sb.append("</RSAKeyValue>");
        return sb.toString().replaceAll("[ \t\n\r]", "");
    }

    private static byte[] removeMSZero(byte[] data) {
        byte[] data1;
        int len = data.length;
        if (data[0] == 0) {
            data1 = new byte[data.length - 1];
            System.arraycopy(data, 1, data1, 0, len - 1);
        } else {
            data1 = data;
        }
        return data1;
    }





    public static void main0(String[] args) {
        String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCz4D01cJbbLdzUprznyrz4bueMWkLZNSBHuxXjynn4WnaELTidvA6280h7WHP+87iNmZAtvrmcEWGPCBvGrNRFzpqtN7c8h6E12SESVWjuF4VkH/tUN/F4UJLtNPEnsmmVAdarwn/c5RJqFVA2sFVlm6Zc2FV3QyPdrdMfa9AizwIDAQAB";
        String PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALPgPTVwltst3NSmvOfKvPhu54xaQtk1IEe7FePKefhadoQtOJ28DrbzSHtYc/7zuI2ZkC2+uZwRYY8IG8as1EXOmq03tzyHoTXZIRJVaO4XhWQf+1Q38XhQku008SeyaZUB1qvCf9zlEmoVUDawVWWbplzYVXdDI92t0x9r0CLPAgMBAAECgYEAqCPLc4G8MkOLsmfuG0njHOMmpIbXCAzmEMcr7hOdse517JYM3z0kEBYXwdzsCP0vnYVXRbuL6vxAUqBEvpFdlhMYDNeDbKlqfWbvAa2RP6stib4OWR85gYbssRn3kh4IY1VWn+GeSbc5ztjSVXKnRbS+ezd0OmXJqiKzPpQtNMECQQDylOWkFeKgegAEzMXM/9VjjgXFoNb8AJVT8QXj2/m4ndL17/n4YHOwbMo0PDy69NKKMDAG3EnTNKBL0xIq2NMhAkEAvdNkMoI7Cedd35xG5bqB+GxWvrZPZN/QHhmQiUGO/CvslHL7QKeit4auDi30g3aUKbo07w/WfxL/me6yJRkn7wJAcXAtv0C4vOCwV45GxWmxqR+GFXf0cN349ssUPQzmR24OdBHnrD22e/8zw5+Tqr3IIvUL0Hl9UHYgq7Sln0HL4QJBAKn0u3Axg5SRb04GyL9kpnt63IuyBRGnBdn9P5h0dwW2egJLlENGE/zHe808PgD6SRu3GS+1eXGa2/jBawSmKkcCQGxLhtbCa08GrcQOHNYrtSfKRn+hJRKvwAWK4K64OGC94spgtPX5H3Ks3QxUGBWAtdlP+OVugfIfZ3Esim+2xSA=";
        Map<String, Object> keyMap;
        try {
            keyMap = createKey();
            String publicKey = getPublicKey(keyMap);
            System.out.println("公钥：\n"+publicKey);
            String privateKey = getPrivateKey(keyMap);
            System.out.println("私钥：\n"+privateKey);
            RSAPublicKey publicKey1 = getPublicKey(PUBLIC_KEY);
            RSAPrivateKey privateKey1 = getPrivateKey(PRIVATE_KEY);
            RSAKeyPair rsaKeyPair = new RSAKeyPair();
            rsaKeyPair.setPublicKey(publicKey1);
            rsaKeyPair.setPrivateKey(privateKey1);
            System.out.println(rsaKeyPair);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main1(String[] args) throws Exception {



        Map<String, Object> keyMap = RSAUtils.createKey();

        byte[] publicKey = RSAUtils.getPublicKeyBytes(keyMap);

        byte[] privateKey = RSAUtils.getPrivateKeyBytes(keyMap);
        System.out.println("公钥：" + publicKey);
        System.out.println("C#公钥：" + getRSAPublicKeyAsNetFormat(publicKey));
        System.out.println("私钥：" + Base64.getEncoder().encodeToString(privateKey));
        System.out.println("C#私钥：" + getRSAPrivateKeyAsNetFormat(privateKey));
        System.out.println("================密钥对构造完毕,甲方将公钥公布给乙方，开始进行加密数据的传输=============");
        String str = "RSA密码交换算法";
        System.out.println("===========甲方向乙方发送加密数据==============");
        System.out.println("原文:" + str);

        byte[] code1 = RSAUtils.encryptByPrivateKey(str.getBytes(), privateKey);
        System.out.println("加密后的数据：" + Base64.getEncoder().encodeToString(code1));
        System.out.println("===========乙方使用甲方提供的公钥对数据进行解密==============");

        byte[] decode1 = RSAUtils.decryptByPublicKey(code1, publicKey);
        System.out.println("乙方解密后的数据：" + new String(decode1));

        System.out.println("===========反向进行操作，乙方向甲方发送数据==============");

        str = "乙方向甲方发送数据RSA算法";

        System.out.println("原文:" + str);


        byte[] code2 = RSAUtils.encryptByPublicKey(str.getBytes(), publicKey);
        System.out.println("===========乙方使用公钥对数据进行加密==============");
        System.out.println("加密后的数据：" + Base64.getEncoder().encodeToString(code2));

        System.out.println("=============乙方将数据传送给甲方======================");
        System.out.println("===========甲方使用私钥对数据进行解密==============");


        byte[] decode2 = RSAUtils.decryptByPrivateKey(code2, privateKey);
        System.out.println("甲方解密后的数据：" + new String(decode2));
    }


    public static void main2(String[] args) throws Exception {
        String data= "N1C/KA/kia3/9pBPbEeE2eHEy8R8wd04E9TDvmWqPZOqEXfq27VK/OoMvvUPnB4wJJnu0S8UjP83ajY3dKZluA==";
        String k = "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAlKbvV+TbA6tAc+q+RTFF5CqNjgqpayrWFCTK1TYtpZ69dFGhIkKnKpT4nTHs9wFyTY8CfnyziWWhneILpoxjpwIDAQABAkAxEzuJBODZQTwyCJlwNmggf3vkHNj5rFaop8zevtgrCM8lTpbtkbDlz/Y90ifhn00eHbaWx4cOXwBVG3g7p6eBAiEA3VsVh6TgE8MMhH2ailRPI5BTKAPKXxB1Fz6qwechg/cCIQCr6uEtvpiwwQVmeMDraufl/AFY1zLmXSIn+YJh8WXR0QIgRws2y8RFDtKpL9TIRuFsTPPDXLJqvzwe+IjqcTVncl0CIQCJ0NPM8QLEhyfGGr1Eu8HFCz0lM/Z412Y/N3S/AV5HUQIhAMTCITa1fmz3nRxH4L7EofGMniZ+xCC0Pk3G1BbxXlSo";
        byte[] decode2 = RSAUtils.decryptByPrivateKey(Base64.getDecoder().decode(data), Base64.getDecoder().decode(k));
        System.out.println("甲方解密后的数据：" + new String(decode2));
    }

    public static void main3(String[] args) throws Exception {
        //私钥
        String privateStr = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAtmEBC5xciJySRAqchSYQR5tnEzsKO/dK0Fg1dVBKKPPwETD5HrQqcDPegRwoiZm8ASpVA2MKZd0iBHFU/M7wNQIDAQABAkEAtK25OWV4jqZ+iQXyNj6VVjtwjC6rXukIpwscOtKGBbalCLgRAs8Q0ZePqe9Duj3/vE8/ZZuTXjSlsJlVSCp/aQIhAPdo8I2aLJrkm/om/CtUHvlW1TCw14eP28zvChQzIx4zAiEAvLYMMVcHD7pe+Xj0hfnc+rmai/64zcjP4VpknqHI//cCIF8bRwWYE7eDU/ZokB1z2+hLme56vI+PHJZ9+Wjkc4aDAiBdJ0Rnir06n1ZIsdOK2yehQMOwfaH+OzWa2YM350cQSwIgOscoD26vCWCF3Q35Tn16RgRYSSyk28s+uqZs1Ld4PvU=";
        System.out.println("java私钥:"+privateStr);
        byte[] privateBytes = Base64.getDecoder().decode(privateStr);

        String dotnetPrivateStr = getRSAPrivateKeyAsNetFormat(privateBytes);
        System.out.println("C#私钥:" + dotnetPrivateStr);

        String publicStr = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALZhAQucXIickkQKnIUmEEebZxM7Cjv3StBYNXVQSijz8BEw+R60KnAz3oEcKImZvAEqVQNjCmXdIgRxVPzO8DUCAwEAAQ==";
        System.out.println("java公钥:"+publicStr);
        byte[] publicBytes = Base64.getDecoder().decode(publicStr);

        String dotnetPublic = getRSAPublicKeyAsNetFormat(publicBytes);
        System.out.println("C#公钥:" + dotnetPublic);

        String content = "{userName:\"admin\", password:\"asdf1234\"}";

        byte[] encryptByPublic = RSAUtils.encryptByPublicKey(content.getBytes(), publicBytes);
        System.out.println("===========甲方使用公钥对数据进行加密==============");
        System.out.println("加密后的数据：" + Base64.getEncoder().encodeToString(encryptByPublic));

        System.out.println("===========甲方使用私钥对数据进行解密==============");

        byte[] decryptByPrivate = RSAUtils.decryptByPrivateKey(encryptByPublic, privateBytes);
        System.out.println("乙方解密后的数据：" + new String(decryptByPrivate));
    }
}
