package com.eachnow.linebot.domain.service.gateway;

import com.eachnow.linebot.common.po.opendata.CpcOilPricePO;

public interface CpcOilApiService {

    /**
     * 取得中油及時油價
     */
    public CpcOilPricePO getOilPrice();

    /**
     * 呼叫取得中油匯率
     */
    public CpcOilPricePO getCurrency();
}
