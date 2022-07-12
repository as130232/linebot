package com.eachnow.linebot.domain.service.gateway;

import com.eachnow.linebot.common.po.javdb.NetflavDataPO;

public interface JavdbApiService {
    public NetflavDataPO search(String code);
}
