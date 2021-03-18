package com.eachnow.linebot.common.po.google.map;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultLocationPO {
    @JsonAlias("html_attributions")
    private List<String> htmlAttributions;
    @JsonAlias("next_pageToken")
    private String nextPageToken;
    private List<ResultPO> results;
}
