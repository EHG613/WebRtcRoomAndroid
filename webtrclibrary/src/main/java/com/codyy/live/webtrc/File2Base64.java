package com.codyy.live.webtrc;

import android.util.Base64;

import org.json.JSONObject;
import org.webrtc.DataChannel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;


public class File2Base64 {
    /**
     * <p>将文件转成base64 字符串</p>
     *
     * @param path                 文件路径
     * @param fileProgressListener
     * @return
     * @throws Exception
     */
    public static void encodeBase64File(DataChannel channel, String path, FileProgressListener fileProgressListener) {
        RandomAccessFile randomAccessFile = null;
        byte[] buffer = new byte[4096 * 10];
        try {
            randomAccessFile = new RandomAccessFile(path, "r");
            //分割文件
            int position = 1;
            long len = 0;
            long total = 0;
            while ((len = randomAccessFile.read(buffer)) != -1 && DataChannel.State.OPEN.equals(channel.state())) {
                JSONObject object = new JSONObject();
                object.put("name", path.substring(path.lastIndexOf("/")));
                object.put("position", position);
                total += len;
                object.put("percent", new BigDecimal(total * 1f / randomAccessFile.length() * 100).intValue());
                object.put("file", base64Encode2String(buffer));
                if (channel.send(new DataChannel.Buffer(
                        ByteBuffer.wrap(object.toString().getBytes()),
                        false))) {
                    fileProgressListener.progress(object.optInt("percent"));
                } else {
                    fileProgressListener.failed("Datachannel closed");
                }
//                    Log.e("channel", );
                position++;
            }
            if (total == randomAccessFile.length()) {
                fileProgressListener.success(path.substring(path.lastIndexOf("/")));
            }
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                fileProgressListener.failed("failed send file to pc,file not found exception " + path);
            } else {
                fileProgressListener.failed("failed send file to pc" + e.getMessage());
            }
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

//    public static void main(String[] args) {
//        try {
//            String base64Code = encodeBase64File("/Users/Crazy/Pictures/zyb2.jpg");
//            System.out.println(base64Code);
//            decoderBase64File(base64Code, "/Users/Crazy/Desktop/zyb.png");
//            toFile(base64Code, "/Users/Crazy/Desktop/zyb.txt");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}