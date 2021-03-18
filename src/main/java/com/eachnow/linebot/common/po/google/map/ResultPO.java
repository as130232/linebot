package com.eachnow.linebot.common.po.google.map;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultPO {
    private String businessStatus;
    private GeometryPO geometry;
    private String icon;
    private String name;
    @JsonAlias("opening_hours")
    private OpeningHoursPO openingHours;
    private List<PhotoPO> photos;
    @JsonAlias("place_id")
    private String placeId;
    @JsonAlias("plus_code")
    private PlusCodePO plusCode;
    private float rating = 0f;      //評分
    private String reference;
    private String scope;
    private List<String> types;
    @JsonAlias("user_ratings_total")
    private Integer userRatingsTotal = 0;   //評論
    private String vicinity = "";   //地址
}
