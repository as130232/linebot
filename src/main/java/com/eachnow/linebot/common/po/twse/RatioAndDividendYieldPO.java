package com.eachnow.linebot.common.po.twse;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RatioAndDividendYieldPO {
    private String code;    //個股代號
    private String name;    //個股名稱
    private Double pbRatio; //股價淨值比
    private Double peRatio; //本益比
    private Double dividendYield;  //殖利率

    private Double price;   //股價
    private Double avePrice;    //月均價
}
