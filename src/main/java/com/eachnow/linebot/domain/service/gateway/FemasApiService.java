package com.eachnow.linebot.domain.service.gateway;

import com.eachnow.linebot.common.po.femas.FemasResultPO;

public interface FemasApiService {
    FemasResultPO getRecords(String token, String searchStart, String searchEnd);
}
