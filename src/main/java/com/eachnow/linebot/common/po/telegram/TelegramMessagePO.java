package com.eachnow.linebot.common.po.telegram;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TelegramMessagePO {

    @JsonAlias("message_id")
    private Integer id;
    @JsonAlias("from")
    private TelegramFromPO from;
    @JsonAlias("chat")
    private Long chat;
    @JsonAlias("date")
    private Long date;
    @JsonAlias("text")
    private String text;
}
