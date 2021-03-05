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
//    @Value("${spring.boot.admin.notify.line.to}")
//    private String to;

}
