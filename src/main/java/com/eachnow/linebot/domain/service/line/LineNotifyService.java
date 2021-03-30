package com.eachnow.linebot.domain.service.line;

import com.eachnow.linebot.common.constant.LineNotifyConstant;
import com.eachnow.linebot.config.LineConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 藉由line notify發送訊息
 * 免費，且沒有訊息限制
 */
@Slf4j
@Component
public class LineNotifyService {
    private RestTemplate restTemplate;
    private LineConfig lineConfig;
    private final String BASE_URL = "https://notify-api.line.me/api/notify";
    @Autowired
    public LineNotifyService(RestTemplate restTemplate, LineConfig lineConfig) {
        this.restTemplate = restTemplate;
        this.lineConfig = lineConfig;
    }

    private HttpHeaders getHttpHeaders(Integer type) {
        String key = lineConfig.getLineNotifyKeyOwn();
        if (LineNotifyConstant.OWN.equals(type)) {
            key = lineConfig.getLineNotifyKeyOwn();
        } else if (LineNotifyConstant.GROUP.equals(type)) {
            key = lineConfig.getLineNotifyKeyGroup();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + key);
        return headers;
    }

    /**
     * 藉由lineNotify發送訊息通知
     * @param type LineNotifyConstant
     * @param message 訊息
     */
    public void send(Integer type, String message) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(null, getHttpHeaders(type));
            ResponseEntity<String> response = restTemplate.exchange(BASE_URL + "?message=" + message,
                    HttpMethod.POST, entity, String.class);
            log.info("[Line Notify]發送訊息，成功。message:{}", message);
        } catch (Exception e) {
            log.error("[Line Notify]發送訊息，失敗! message:{}, error msg:{}", message, e.getMessage());
        }
    }


}
