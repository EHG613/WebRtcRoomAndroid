package com.codyy.live.share;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 资源类型
 */
@Retention(RetentionPolicy.SOURCE)
public @interface ResType {
    /**
     * 文件夹
     */
    String dir = "dir";
    String parentDir = "parentDir";
    /**
     * 音频
     */
    String aac = "aac";
    String mp3 = "mp3";
    String oga = "oga";
    String wav = "wav";
    String weba = "weba";
    /**
     * 视频
     */
    String mp4 = "mp4";
    String avi = "avi";
    String mpeg = "mpeg";
    String ogv = "ogv";
    String webm = "webm";
    String mkv = "mkv";
    String threeGP = "3gp";
    String threeG2 = "3g2";
    /**
     * 压缩
     */
    String zip = "zip";
    String sevenZ = "7z";
    /**
     * 图片
     */
    String bmp = "bmp";
    String gif = "gif";
    String ico = "ico";
    String jpg = "jpg";
    String jpeg = "jpeg";
    String png = "png";
    String webp = "webp";


    /**
     * win 安装包
     */
    String exe = "exe";
    /**
     * 网页
     */
    String htm = "htm";
    String html = "html";
    /**
     * pdf
     */
    String pdf = "pdf";
    /**
     * txt
     */
    String ics = "ics";
    String csv = "csv";
    String css = "css";
    String txt = "txt";
    /**
     * office type
     */
    String doc = "doc";
    String docx = "docx";
    String xls = "xls";
    String xlsx = "xlsx";
    String ppt = "ppt";
    String pptx = "pptx";
    String vsd = "vsd";
    /**
     * 其他类型
     */
    String ai = "ai";
    String unknow = "unknow";
    String xml="xml";
}
