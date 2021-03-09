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

    public void send(String to, String type, String text) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + lineConfig.getChannelToken());
        List<MessagePO> messages = new ArrayList<>();
        messages.add(MessagePO.builder().type(type).text(text).build());
        PushMessagePO pushMessagePO = PushMessagePO.builder().to(to).messages(messages).build();
        HttpEntity<String> entity = new HttpEntity<>(JsonUtils.toJsonString(pushMessagePO), headers);
        ResponseEntity<String> response = restTemplate.exchange("https://api.line.me/v2/bot/message/push",
                HttpMethod.POST, entity, String.class);
        log.info("發送訊息，成功。to:{}, type:{}, text:{}", to, type, text);
    }
}
