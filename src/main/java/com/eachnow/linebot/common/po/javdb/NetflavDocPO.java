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

    public String getTitle() {
        if (title != null && title.contains("[")) {
            return title.substring(title.indexOf("]") + 1);
        }
        return "";
    }

    public String getCode() {
        if (title != null && title.contains("[")) {
            return title.substring(title.indexOf("[") + 1, title.indexOf("]"));
        } else if (title != null && title.contains(" ")) {
            String[] titleArr = title.split(" ");
            return titleArr[0];
        }
        return null;
    }

    public String getAuthor() {
        if (title != null && title.contains(" ")) {
            String[] titleArr = title.split(" ");
            return titleArr[titleArr.length - 1];
        }
        return null;
    }

    public String getWebUrl() {
        String code = getCode();
        if (code != null) {
            return "https://jable.tv/videos/{code}/".replace("{code}", code);
        }
        return "https://jable.tv/new-release/";
    }

    public String getDate() {
        if (sourceDate != null) {
            return sourceDate.split("T")[0].replace("-", "/");
        }
        return null;
    }
}
