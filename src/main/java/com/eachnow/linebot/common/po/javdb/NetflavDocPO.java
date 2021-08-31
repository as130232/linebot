package com.eachnow.linebot.common.po.javdb;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class NetflavDocPO {
    private String preview; //圖片URL
    private List<String> previewImages;
    private String sourceDate;
    private List<String> actors;
    private String createdAt;
    private String videoId;
    private String title_en;
    private String preview_hp;
    private String title;
    private String views;
    private String status;
    private String title_zh;
    private String _id;
}
