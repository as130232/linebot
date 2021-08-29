package com.eachnow.linebot.domain.service.gateway;

import com.eachnow.linebot.common.constant.PttEnum;
import com.eachnow.linebot.common.po.PttInfoPO;

public interface PttApiService {
    PttInfoPO getPttInfoPO(PttEnum pttEnum, int size);
}
