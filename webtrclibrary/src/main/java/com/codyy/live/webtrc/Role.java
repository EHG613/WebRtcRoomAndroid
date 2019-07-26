/*
 * 阔地教育科技有限公司版权所有(codyy.com/codyy.cn)
 * Copyright (c) 2019, Codyy and/or its affiliates. All rights reserved.
 */

package com.codyy.live.webtrc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface Role {
    /**
     * PC端
     */
    String PC="pc";
    /**
     * 授课端 or 控制端
     */
    String CTRL="ctrl";
    /**
     * 客户端 or 学生端
     */
    String CLIENT="client";

}
