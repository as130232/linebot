package com.eachnow.linebot.domain.service.gateway;

import com.eachnow.linebot.common.po.fugle.FugleChartPO;
import com.eachnow.linebot.common.po.fugle.FugleDealtsPO;
import com.eachnow.linebot.common.po.fugle.FugleMetaPO;
import com.eachnow.linebot.common.po.fugle.FugleQuotePO;

public interface FugleApiService {

    /**
     * 提供盤中個股/指數 線圖時所需的各項即時資訊
     */
    public FugleChartPO getChart(Integer symbolId);
    /**
     * 提供盤中個股/指數逐筆交易金額、狀態、最佳五檔及統計資訊
     */
    public FugleQuotePO getQuote(Integer symbolId);
    /**
     * 取得盤中個股/指數當日基本資訊
     */
    public FugleMetaPO getMeta(Integer symbolId);
    /**
     * 取得個股當日所有成交資訊（ex: 個股價量、大盤總量）
     */
    public FugleDealtsPO getDealts(Integer symbolId);
}
