package com.eachnow.linebot.common.po.twse;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PricePO {
    private String code;    //個股代號
    private String name;    //個股名稱
    private String price;   //當日收盤價
    private String avePrice;  //月平均價
}
