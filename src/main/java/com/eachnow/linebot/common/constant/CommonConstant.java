package com.eachnow.linebot.common.constant;

import lombok.Getter;

@Getter
public class CommonConstant {
    //table:remind cloumn valid status
    public static final Integer INVALID = 0;    //無效
    public static final Integer VALID = 1;      //有效
    public static final Integer DONE = 2;       //已完成
    //table:remind cloumn type status
    public static final Integer ONCE = 1;       //一次性
    public static final Integer CONTINUOUS = 2; //持續性
}
