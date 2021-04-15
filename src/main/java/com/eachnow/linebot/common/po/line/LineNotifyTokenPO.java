package com.eachnow.linebot.common.po.line;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;

@Getter
public class LineNotifyTokenPO {
    private Integer status;
    private String message;
    @JsonAlias("access_token")
    private String accessToken;
}
