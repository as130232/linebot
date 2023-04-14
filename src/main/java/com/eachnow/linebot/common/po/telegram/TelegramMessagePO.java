package com.eachnow.linebot.common.po.telegram;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TelegramMessagePO {

    @JsonAlias("message_id")
    private Integer id;
    @JsonAlias("update_id")
    private TelegramFromPO from;
    @JsonAlias("update_id")
    private Long chat;
    private Long date;
    private String text;
}
