package com.eachnow.linebot.common.po;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CommandPO {
    private String text;    //command + params
    private String command; //指令
    private List<String> params;    //除指令外的參數
    private DatetimepickerPO datetimepicker;   //datetimepicker時間參數
    private String userId;
}
