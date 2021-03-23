package com.eachnow.linebot.domain.service.line;

import com.eachnow.linebot.common.constant.LineNotifyEnum;
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

    @Autowired
    public LineNotifyService(RestTemplate restTemplate, LineConfig lineConfig) {
        this.restTemplate = restTemplate;
        this.lineConfig = lineConfig;
    }

    private HttpHeaders getHttpHeaders(LineNotifyEnum lineNotifyEnum) {
        String key = lineConfig.getLineNotifyKeyOwn();
        if (LineNotifyEnum.OWN.equals(lineNotifyEnum)) {
            key = lineConfig.getLineNotifyKeyOwn();
        } else if (LineNotifyEnum.GROUP.equals(lineNotifyEnum)) {
            key = lineConfig.getLineNotifyKeyGroup();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", key);
        return headers;
    }

    public void send(LineNotifyEnum lineNotifyEnum, String message) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(null, getHttpHeaders(lineNotifyEnum));
            ResponseEntity<String> response = restTemplate.exchange("https://notify-api.line.me/api/notify",
                    HttpMethod.POST, entity, String.class);
            log.info("[Line Notify]發送訊息，成功。message:{}", message);
        } catch (Exception e) {
            log.error("[Line Notify]發送訊息，失敗! message:{}, error msg:{}", message, e.getMessage());
        }
    }


}
