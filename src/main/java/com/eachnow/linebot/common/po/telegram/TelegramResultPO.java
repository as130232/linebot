package com.eachnow.linebot.common.po.telegram;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TelegramResultPO {
    @JsonAlias("update_id")
    private Long updateId;
    private TelegramMessagePO message;
}
