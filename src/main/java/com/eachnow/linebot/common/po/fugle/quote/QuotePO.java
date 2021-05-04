package com.eachnow.linebot.common.po.fugle.quote;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuotePO {
    private Boolean isCurbing;  //最近一次更新是否為瞬間價格穩定措施
    private Boolean isCurbingRise;  //最近一次更新是否為暫緩撮合且瞬間趨漲
    private Boolean isCurbingFall;  //最近一次更新是否為暫緩撮合且瞬間趨跌
    private Boolean isTrial;        //最近一次更新是否為試算
    private Boolean isOpenDelayed;  //當日是否曾發生延後開盤
    private Boolean isCloseDelayed; //當日是否曾發生延後收盤
    private Boolean isHalting;      //最近一次更新是否為暫停交易
    private Boolean isClosed;       //當日是否為已收盤
    //暫未提供
    private Integer change;         //當日股價之漲跌
    private Integer changePercent;  //當日股價之漲跌幅
    private Double amplitude;       //當日股價之振幅
    private String priceLimit;

    private TotalPO total;  //總成交量(不包含盤後14:30的量)
    private TrialPO trial;  //最新一筆"試撮"價格與成交量
    private TradePO trade;  //最新一筆"成交"價格與成交量(當日收盤價)
    private OrderPO order;  //五檔價格與委託張數

    private PricePO priceHigh;  //當日最高價
    private PricePO priceLow;   //當日最低價
    private PricePO priceOpen;  //當日開盤價
}
