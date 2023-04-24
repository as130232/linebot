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
public class LineNotifySender {
    private RestTemplate restTemplate;
    private LineConfig lineConfig;
    private final String BASE_URL = "https://notify-api.line.me/api/notify";
    @Autowired
    public LineNotifySender(RestTemplate restTemplate, LineConfig lineConfig) {
        this.restTemplate = restTemplate;
        this.lineConfig = lineConfig;
    }

    private HttpHeaders getHttpHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    /**
     * 藉由lineNotify發送訊息通知
     * @param token Line Notify Token
     * @param message 訊息
     */
    public void send(String token, String message) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(null, getHttpHeaders(token));
            ResponseEntity<String> response = restTemplate.exchange(BASE_URL + "?message=" + message,
                    HttpMethod.POST, entity, String.class);
            log.info("[Line Notify]發送訊息，成功。message:{}", message);
        } catch (Exception e) {
            log.error("[Line Notify]發送訊息，失敗! message:{}, error msg:{}", message, e.getMessage());
        }
    }

    public void sendToCharles(String message){
        send(lineConfig.getLineNotifyKeyOwn(), message);
    }

}
