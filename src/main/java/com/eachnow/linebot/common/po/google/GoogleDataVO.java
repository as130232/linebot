package com.eachnow.linebot.common.po.google;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class GoogleDataVO {
    private List<GoogletTranslationsPO> translations;
}
