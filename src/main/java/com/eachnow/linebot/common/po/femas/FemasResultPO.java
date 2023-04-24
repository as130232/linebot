package com.eachnow.linebot.common.po.femas;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class FemasResultPO {
    private FemasResponsePO response;
}
