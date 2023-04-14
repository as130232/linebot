package com.eachnow.linebot.common.po.telegram;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TelegramChatPO {
    @JsonAlias("id")
    private Integer id;
    @JsonAlias("first_name")
    private String firstName;
    @JsonAlias("last_name")
    private String lastName;
    @JsonAlias("type")
    private String type;
}
