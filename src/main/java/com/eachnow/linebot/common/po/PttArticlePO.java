package com.eachnow.linebot.common.po;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class PttArticlePO {
    private String title;
    private Integer popularity = 0;
    private String webUrl;
    private String pictureUrl;
    private String author;
    private String date;
    private Set<String> pictures;
}
