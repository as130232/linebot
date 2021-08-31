package com.eachnow.linebot.common.po.javdb;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticlePO {
    private String title;
    private String webUrl;
    private String pictureUrl;
    private String author;
    private String date;
    private String code;
}
