package com.eachnow.linebot.common.po.ig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MediaCollections {
    private int count;

    @JsonProperty("page_info")
    private PageInfo pageInfo;

    private List<Edge> edges;
}