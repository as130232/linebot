package com.eachnow.linebot.common.po.fugle;

import com.eachnow.linebot.common.po.fugle.meta.MetaDataPO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FugleMetaPO {
    private String apiVersion;
    private MetaDataPO data;
}
