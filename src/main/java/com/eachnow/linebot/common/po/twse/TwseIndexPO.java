package com.eachnow.linebot.common.po.twse;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 大盤與各類指數
 */
@Data
@NoArgsConstructor
public class TwseIndexPO {
    private IndexPO taiex;   //大盤指數
    private List<IndexPO> categories;    //各類指數名稱
}
