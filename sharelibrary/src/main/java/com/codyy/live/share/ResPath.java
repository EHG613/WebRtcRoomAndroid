package com.codyy.live.share;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 资源路径
 */
@Retention(RetentionPolicy.SOURCE)
public @interface ResPath {
    String DOWNLOAD="Downloads";
    String DESKTOP="Desktop";
    String DOCUMENTS ="Documents";
    String PATH="path";
}
