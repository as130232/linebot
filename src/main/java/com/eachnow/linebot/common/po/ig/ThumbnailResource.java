package com.eachnow.linebot.common.po.ig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThumbnailResource {

    private String src;

    @JsonProperty("config_width")
    private int configWidth;

    @JsonProperty("config_height")
    private int configHeight;

}