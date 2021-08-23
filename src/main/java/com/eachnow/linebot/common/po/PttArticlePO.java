package com.eachnow.linebot.common.po;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PttArticlePO {
    private String title;
    private Integer popularity;
    private String webUrl;
    private String pictureUrl;
    private String author;
    private String date;
}
