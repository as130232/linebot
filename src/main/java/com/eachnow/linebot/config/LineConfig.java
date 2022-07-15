package com.eachnow.linebot.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class LineConfig {
    private boolean enabled = true;
    @Value("${line.bot.channel-secret}")
    private String channelSecret;
    @Value("${line.bot.channel-token}")
    private String channelToken;
    @Value("${line.notify.key.own}")
    private String lineNotifyKeyOwn;    //charles notify token
    @Value("${line.notify.key.group}")
    private String lineNotifyKeyGroup;
    @Value("${line.notify.client-id}")
    private String lineNotifyClientId;
    @Value("${line.notify.client-secret}")
    private String lineNotifyClientSecret;
}
