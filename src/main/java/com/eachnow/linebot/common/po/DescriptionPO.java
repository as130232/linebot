package com.eachnow.linebot.common.po;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DescriptionPO {
    private String title;
    private String description;
    private List<DescriptionCommandPO> commands;
    private String imageUrl;
}
