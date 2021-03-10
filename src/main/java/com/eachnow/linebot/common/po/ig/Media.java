package com.eachnow.linebot.common.po.ig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Media {
    private List<Node> nodes;

    private int count;

    @JsonProperty("page_info")
    private PageInfo pageInfo;
}
