package com.codyy.live.webtrc;

import android.util.Base64;


import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class File2Base64 {
    /**
     * <p>将文件转成base64 字符串</p>
     *
     * @param path 文件路径
     * @return
     * @throws Exception
     */
    public static String encodeBase64File(String path) throws Exception {
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);
        inputFile.close();
        JSONObject object=new JSONObject();
        object.put("name",file.getName());
        object.put("file",base64Encode2String(buffer));
        return object.toString();
    }

    /**
     * <p>将base64字符解码保存文件</p>
     *
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    public static void decoderBase64File(String base64Code, String targetPath) throws Exception {
        byte[] buffer = base64Decode(base64Code);
        FileOutputStream out = new FileOutputStream(targetPath);
        out.write(buffer);
        out.close();
    }
    /**
     * Return Base64-encode string.
     *
     * @param input The input.
     * @return Base64-encode string
     */
    public static String base64Encode2String(final byte[] input) {
        if (input == null || input.length == 0) return "";
        return Base64.encodeToString(input, Base64.NO_WRAP);
    }

    /**
     * Return the bytes of decode Base64-encode string.
     *
     * @param input The input.
     * @return the string of decode Base64-encode string
     */
    public static byte[] base64Decode(final String input) {
        if (input == null || input.length() == 0) return new byte[0];
        return Base64.decode(input, Base64.NO_WRAP);
    }
    /**
     * <p>将base64字符保存文本文件</p>
     *
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    public static void toFile(String base64Code, String targetPath) throws Exception {
        byte[] buffer = base64Code.getBytes();
        FileOutputStream out = new FileOutputStream(targetPath);
        out.write(buffer);
        out.close();
    }

    public static void main(String[] args) {
        try {
            String base64Code = encodeBase64File("/Users/Crazy/Pictures/zyb2.jpg");
            System.out.println(base64Code);
            decoderBase64File(base64Code, "/Users/Crazy/Desktop/zyb.png");
            toFile(base64Code, "/Users/Crazy/Desktop/zyb.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}