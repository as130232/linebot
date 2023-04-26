package com.eachnow.linebot.domain.service.line;

import com.eachnow.linebot.common.po.MessagePO;
import com.eachnow.linebot.common.po.PushMessagePO;
import com.eachnow.linebot.common.util.JsonUtils;
import com.eachnow.linebot.config.LineConfig;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.response.BotApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class MessageSender {
    private final String PUSH_URL = "https://api.line.me/v2/bot/message/push";
    private RestTemplate restTemplate;
    private LineConfig lineConfig;
    private LineMessagingClient client;

    @Autowired
    public MessageSender(RestTemplate restTemplate, LineConfig lineConfig) {
        this.restTemplate = restTemplate;
        this.lineConfig = lineConfig;
        client = LineMessagingClient.builder(lineConfig.getChannelToken()).build();
    }

//    @PostConstruct
//    private void test(){
//        Message message = messageHandler.executeCommand("test", "匯率", null);
//        Message message2 = messageHandler.executeCommand("test", "中油", null);
//        List<Message> messages = new ArrayList<>();
//        messages.add(message);
//        messages.add(message2);
//        BotApiResponse botApiResponse = push("Uf52a57f7e6ba861c05be8837bfbcf0c6", messages);
//        System.out.println(botApiResponse);
//    }

    public BotApiResponse push(String to, List<Message> messages){
        PushMessage pushMessage = new PushMessage(to, messages);
        try {
            return client.pushMessage(pushMessage).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("發送訊息，失敗! to:{}, messages:{}, error msg:{}", to, messages, e.getMessage());
            return null;
        }
    }

    /**
     * @param to line token
     * @param type: text, sticker, image..(com.linecorp.bot.model.message.Message.@Type)
     */
    public void pushByRest(String to, String type, String text) {
        List<MessagePO> messages = new ArrayList<>();
        messages.add(MessagePO.builder().type(type).text(text).build());
        PushMessagePO pushMessagePO = PushMessagePO.builder().to(to).messages(messages).build();
        pushByRest(pushMessagePO);
    }

    public void pushByRest(PushMessagePO pushMessagePO) {
        try {
            String json = JsonUtils.toJsonString(pushMessagePO);
            HttpEntity<String> entity = new HttpEntity<>(json, getHttpHeaders());
            ResponseEntity<String> response = restTemplate.exchange(PUSH_URL, HttpMethod.POST, entity, String.class);
            log.info("發送訊息，成功。json:{}", json);
        } catch (Exception e) {
            log.error("發送訊息，失敗! pushMessagePO:{}, error msg:{}", pushMessagePO, e.getMessage());
        }
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + lineConfig.getChannelToken());
        return headers;
    }

}
