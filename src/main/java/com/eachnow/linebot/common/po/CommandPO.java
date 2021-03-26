package com.eachnow.linebot.common.po;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CommandPO {
    private String text;    //command + params
    private String command; //指令
    private List<String> params;    //除指令外的參數
    private String userId;
}
