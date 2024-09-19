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

    public FemasPunchRecordPO getPunchRecord(String date, String userName) {
        return PUNCH_RECORD_CACHE.getIfPresent(getKey(date, userName));
    }

    public void setPunchRecord(String date, String userName, FemasPunchRecordPO po) {
        PUNCH_RECORD_CACHE.put(getKey(date, userName), po);
    }

    public boolean isRecordExist(String date, String userName) {
        return null != PUNCH_RECORD_CACHE.getIfPresent(getKey(date, userName));
    }

    public void removeRecord(String date, String userName) {
        PUNCH_RECORD_CACHE.asMap().remove(getKey(date, userName));
    }

    private String getKey(String date, String userName) {
        userName = userName.toLowerCase();
        return (date + "_" + userName).toLowerCase();
    }

}
