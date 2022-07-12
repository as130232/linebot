package com.eachnow.linebot.common.po.google.map;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class PhotoPO {
    private String height;
    @JsonAlias("html_attributions")
    private List<String> htmlAttributions;
    @JsonAlias("photo_reference")
    private String photoReference;
    private String width;
}
