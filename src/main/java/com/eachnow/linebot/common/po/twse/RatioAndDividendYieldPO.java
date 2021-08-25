package com.eachnow.linebot.common.po.twse;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RatioAndDividendYieldPO {
    private String code;    //個股代號
    private String name;    //個股名稱
    private String peRatio; //本益比
    private String pbRatio; //股價淨值比
    private String dividendYield;  //殖利率
}
