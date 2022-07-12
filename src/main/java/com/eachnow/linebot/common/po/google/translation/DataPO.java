package com.eachnow.linebot.common.po.google.translation;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DataPO {
    private List<TranslationPO> translations;
}
