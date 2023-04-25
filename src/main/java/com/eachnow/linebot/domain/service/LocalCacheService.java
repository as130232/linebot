package com.eachnow.linebot.domain.service;

import com.eachnow.linebot.common.po.femas.FemasPunchRecordPO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LocalCacheService {

    //key: date, value:上下班紀錄
    private static final Cache<String, FemasPunchRecordPO> PUNCH_RECORD_CACHE = Caffeine.newBuilder().expireAfterAccess(15, TimeUnit.HOURS).build();

    public FemasPunchRecordPO getPunchRecord(String date) {
        return PUNCH_RECORD_CACHE.getIfPresent(date);
    }

    public void setPunchRecord(String date, FemasPunchRecordPO po){
        PUNCH_RECORD_CACHE.put(date, po);
    }

    public boolean isRecordExist(String date){
        return null != PUNCH_RECORD_CACHE.getIfPresent(date);
    }

    public void removeRecord(String date){
        PUNCH_RECORD_CACHE.asMap().remove(date);
    }

}
