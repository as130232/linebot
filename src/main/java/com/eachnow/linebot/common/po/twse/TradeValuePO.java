package com.eachnow.linebot.common.po.twse;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TradeValuePO {
    private String item;        //單位名稱
    private Double totalBuy;    //買進金額
    private Double totalSell;   //賣出金額
    private Double difference;  //買賣差額

    private Double balance;     //今日餘額
    private Double balanceOfPreDay; //前日餘額
}
