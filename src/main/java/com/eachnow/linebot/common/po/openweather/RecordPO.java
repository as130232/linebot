package com.eachnow.linebot.common.po.openweather;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RecordPO {
    private String datasetDescription;
    private List<LocationPO> location;
}
