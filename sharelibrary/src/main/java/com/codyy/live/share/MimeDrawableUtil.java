package com.codyy.live.share;

import android.widget.ImageView;

public class MimeDrawableUtil {
    public static boolean isSupportedMimeToOpen( String mime) {
        boolean isSupportedMimeToOpen=false;
        switch (mime) {
            case ResType.sevenZ:
            case ResType.zip:
                break;
            case ResType.pptx:
            case ResType.ppt:
            case ResType.docx:
            case ResType.doc:
            case ResType.xls:
            case ResType.xlsx:
                isSupportedMimeToOpen=true;
                break;
            case ResType.pdf:
                break;
            case ResType.vsd:
                break;
            case ResType.ics:
            case ResType.csv:
            case ResType.css:
            case ResType.txt:
                break;
            case ResType.htm:
            case ResType.html:
                break;
            case ResType.exe:
                break;
            case ResType.parentDir:
            case ResType.dir:
                break;
            case ResType.mp4:
            case ResType.avi:
            case ResType.mpeg:
            case ResType.mkv:
            case ResType.ogv:
            case ResType.webm:
            case ResType.threeG2:
            case ResType.threeGP:
                isSupportedMimeToOpen=true;
                break;
            case ResType.aac:
            case ResType.mp3:
            case ResType.oga:
            case ResType.wav:
            case ResType.weba:
                isSupportedMimeToOpen=true;
                break;
            case ResType.ai:
                break;
            case ResType.bmp:
            case ResType.gif:
            case ResType.ico:
            case ResType.jpg:
            case ResType.jpeg:
            case ResType.webp:
            case ResType.png:
                isSupportedMimeToOpen=true;
                break;
            default:
                break;

        }
        return isSupportedMimeToOpen;
    }
    public static void setMimeDrawable(ImageView imageView, String mime) {
        switch (mime) {
            case ResType.sevenZ:
            case ResType.zip:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.zip));
                break;
            case ResType.pptx:
            case ResType.ppt:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.ppt));
                break;
            case ResType.pdf:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.pdf));
                break;
            case ResType.docx:
            case ResType.doc:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.word));
                break;
            case ResType.xls:
            case ResType.xlsx:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.excel));
                break;
            case ResType.vsd:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.visio));
                break;
            case ResType.ics:
            case ResType.csv:
            case ResType.css:
            case ResType.txt:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.txt));
                break;
            case ResType.htm:
            case ResType.html:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.html));
                break;
            case ResType.exe:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.exe));
                break;
            case ResType.parentDir:
            case ResType.dir:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.folder));
                break;
            case ResType.mp4:
            case ResType.avi:
            case ResType.mpeg:
            case ResType.ogv:
            case ResType.mkv:
            case ResType.webm:
            case ResType.threeG2:
            case ResType.threeGP:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.mp4));
                break;
            case ResType.aac:
            case ResType.mp3:
            case ResType.oga:
            case ResType.wav:
            case ResType.weba:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.audio));
                break;
            case ResType.ai:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.ai));
                break;
            case ResType.bmp:
            case ResType.gif:
            case ResType.ico:
            case ResType.jpg:
            case ResType.jpeg:
            case ResType.webp:
            case ResType.png:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.image));
                break;
            default:
                imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.unknown));
                break;

        }
    }
}
