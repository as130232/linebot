package com.eachnow.linebot.common.po.twse;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PricePO {
    private String code;    //個股代號
    private String name;    //個股名稱
    private Double price;   //當日收盤價
    private Double avePrice;  //月平均價
}
