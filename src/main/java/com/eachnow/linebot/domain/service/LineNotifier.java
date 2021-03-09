package com.eachnow.linebot.domain.service;

import com.eachnow.linebot.common.po.SimpleContentPO;
import com.eachnow.linebot.common.po.SimplePushPO;
import com.eachnow.linebot.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class LineNotifier {
    private RestTemplate restTemplate;

    @Autowired
    public LineNotifier(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void send(String text) throws Exception {

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
        SimplePushPO simplePushPO = SimplePushPO.builder()
                .content(SimpleContentPO.builder().type("text").text(text).build())
                .build();
        HttpEntity<String> entity = new HttpEntity<>(JsonUtils.toJsonString(simplePushPO), headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.line.me/v2/bot/message/push",
                HttpMethod.POST, entity, String.class);

    }
}
