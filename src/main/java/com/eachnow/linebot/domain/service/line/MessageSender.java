package com.eachnow.linebot.domain.service.line;

import com.eachnow.linebot.common.po.MessagePO;
import com.eachnow.linebot.common.po.PushMessagePO;
import com.eachnow.linebot.common.util.JsonUtils;
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

    @Autowired
    public MessageSender(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void send(String to, String type, String text) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
//        Map<String, String> parameter = new HashMap<>();
//        parameter.put("to", "");
//        List<String> messages = new ArrayList();
//        HashMap message = new HashMap<>();
//        message.put("type", "text");
//        message.put("text", text);
//        messages.add(message);
//        parameter.put("messages", messages);
        List<MessagePO> messages = new ArrayList<>();
        messages.add(MessagePO.builder().type(type).text(text).build());
        PushMessagePO pushMessagePO = PushMessagePO.builder().to(to).messages(messages).build();
        HttpEntity<String> entity = new HttpEntity<>(JsonUtils.toJsonString(pushMessagePO), headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.line.me/v2/bot/message/push",
                HttpMethod.POST, entity, String.class);
    }
}
