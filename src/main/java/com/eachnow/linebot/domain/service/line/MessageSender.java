package com.eachnow.linebot.domain.service.line;

import com.eachnow.linebot.common.po.MessagePO;
import com.eachnow.linebot.common.po.PushMessagePO;
import com.eachnow.linebot.common.util.JsonUtils;
import com.eachnow.linebot.config.LineConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MessageSender {
    private RestTemplate restTemplate;
    private LineConfig lineConfig;

    @Autowired
    public MessageSender(RestTemplate restTemplate, LineConfig lineConfig) {
        this.restTemplate = restTemplate;
        this.lineConfig = lineConfig;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + lineConfig.getChannelToken());
        return headers;
    }

    public void send(String to, String type, String text) throws Exception {
        List<MessagePO> messages = new ArrayList<>();
        messages.add(MessagePO.builder().type(type).text(text).build());
        PushMessagePO pushMessagePO = PushMessagePO.builder().to(to).messages(messages).build();
        this.send(pushMessagePO);
    }

    public void send(PushMessagePO pushMessagePO) throws Exception {
        try{
            HttpEntity<String> entity = new HttpEntity<>(JsonUtils.toJsonString(pushMessagePO), getHttpHeaders());
            ResponseEntity<String> response = restTemplate.exchange("https://api.line.me/v2/bot/message/push",
                    HttpMethod.POST, entity, String.class);
            log.info("發送訊息，成功。pushMessagePO:{}", pushMessagePO);
        }catch (Exception e){
            log.error("發送訊息，失敗! pushMessagePO:{}, error msg:{}", pushMessagePO, e.getMessage());
        }
    }

}
