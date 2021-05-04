package com.eachnow.linebot.common.po.fugle.meta;

import com.eachnow.linebot.common.po.fugle.InfoPO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MetaDataPO {
    private InfoPO info;
    private MetaPO meta;
}
