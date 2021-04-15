package com.eachnow.linebot.domain.service.gateway;

public interface LineApiService {
    /**
     * 根據授權碼取得line notify token
     * @param code 授權碼
     * @return line notify token
     */
    public String getLineNotifyToken(String code);
}
