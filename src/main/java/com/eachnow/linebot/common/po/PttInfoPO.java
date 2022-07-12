package com.eachnow.linebot.common.po;

import com.eachnow.linebot.common.constant.PttEnum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PttInfoPO {
    private PttEnum pttEnum;
    private String name;
    private String link;
    private String pageUpLink;
    private Integer boardPopularity = 0;
    private List<PttArticlePO> articles;
}
