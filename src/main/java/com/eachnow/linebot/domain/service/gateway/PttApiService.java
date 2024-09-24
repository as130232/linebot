package com.eachnow.linebot.domain.service.gateway;

import com.eachnow.linebot.common.constant.PttEnum;
import com.eachnow.linebot.common.po.PttInfoPO;

import java.util.List;
import java.util.Set;

public interface PttApiService {
    PttInfoPO getPttInfoPO(PttEnum pttEnum, int size);

    Set<String> listPicture(String url);
}
