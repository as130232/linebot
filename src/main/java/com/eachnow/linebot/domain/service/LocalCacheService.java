package com.eachnow.linebot.domain.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LocalCacheService {
    private static final Cache<Integer, String> MATCH_CLOSE_CACHE = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

    public void setMatchClose(Integer mid) {
        MATCH_CLOSE_CACHE.put(mid, new String());
    }
    public void removeMatchClose(Integer mid) {
        MATCH_CLOSE_CACHE.asMap().remove(mid);
    }
    public boolean isMatchClose(Integer mid) {
        return null != MATCH_CLOSE_CACHE.getIfPresent(mid);
    }
    public String getMatchOddsTime(Integer mid) {
        return MATCH_CLOSE_CACHE.getIfPresent(mid);
    }

}
