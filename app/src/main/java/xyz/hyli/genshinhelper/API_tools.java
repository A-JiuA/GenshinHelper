package xyz.hyli.genshinhelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Random;

public class API_tools {
    // 生成32位随机十六进制数
    // 不知道咋写 随便实现的
    public static String random_hex() {
        Random rand = new Random();
        String result1 = Integer.toHexString(rand.nextInt(256 * 256)).toUpperCase();
        String result2 = Integer.toHexString(rand.nextInt(256 * 256)).toUpperCase();
        String result3 = Integer.toHexString(rand.nextInt(256 * 256)).toUpperCase();
        String result4 = Integer.toHexString(rand.nextInt(256 * 256)).toUpperCase();
        String result5 = Integer.toHexString(rand.nextInt(256 * 256)).toUpperCase();
        String result6 = Integer.toHexString(rand.nextInt(256 * 256)).toUpperCase();
        String result7 = Integer.toHexString(rand.nextInt(256 * 256)).toUpperCase();
        String result8 = Integer.toHexString(rand.nextInt(256 * 256)).toUpperCase();
        String result = result1 + result2 + result3 + result4 + result5 + result6 + result7 + result8;
        return String.join("", Collections.nCopies(32 - result.length(), "0")) + result;
    }

    // 字符串的md5值
    public static String md5(String text) {
        if (text == null || text.length() == 0) {
            return null;
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(text.getBytes());
            byte[] byteArray = md5.digest();
            char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
            char[] charArray = new char[byteArray.length * 2];
            int index = 0;
            for (byte b : byteArray) {
                charArray[index++] = hexDigits[b >>> 4 & 0xf];
                charArray[index++] = hexDigits[b & 0xf];
            }
            return new String(charArray);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取的DS
    public static String DSGet(String q, String b) {
        // q=需要传参的值
        // b=body的值
        Random rand = new Random();
        String t = Long.toString(System.currentTimeMillis() / 1000);
        int r = rand.nextInt(100000) + 100000;
        String c;
        if (q == "" && b == "") {
            String s = "h8w582wxwgqvahcdkpvdhbh2w9casgfl";
            c = md5("salt=" + s + "&t=" + t + "&r=" + r);
        } else {
            String s = "xV8v4Qu54lUKrEYFZkJhB8cuOh9Asafs";
            c = md5("salt=" + s + "&t=" + t + "&r=" + r + "&b=" + b + "&q=" + q);
        }

        return t + "," + r + "," + c;
    }
}
