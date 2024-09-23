package com.eachnow.linebot.domain.service.gateway;

import com.eachnow.linebot.common.po.femas.FemasPayResultPO;
import com.eachnow.linebot.common.po.femas.FemasPunchResultPO;

public interface FemasApiService {
    /**
     * 取得打卡記錄
     */
    FemasPunchResultPO getPunchRecords(String token, String searchStart, String searchEnd);

    /**
     * 取得薪資紀錄
     */
    FemasPayResultPO getPayrollRecords(String token, String yearMonth);
}
