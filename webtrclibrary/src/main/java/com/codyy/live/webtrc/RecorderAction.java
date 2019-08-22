package com.codyy.live.webtrc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 录制动作
 */
@Retention(RetentionPolicy.SOURCE)
public @interface RecorderAction {
    String START="start";
    String PAUSE="pause";
    String RESUME="resume";
    String STOP="stop";

}
