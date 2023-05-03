package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.Result;
import com.eachnow.linebot.common.po.femas.FemasPunchRecordPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.FemasService;
import com.eachnow.linebot.domain.service.LocalCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/femas")
public class FemasController {
    private final FemasService femasService;

    @Autowired
    public FemasController(FemasService femasService) {
        this.femasService = femasService;
    }

    @GetMapping(value = "/punch/record")
    public Result<FemasPunchRecordPO> switchCron(@RequestParam(value = "date", required = false) String date) {
        date = Objects.isNull(date) ? DateUtils.getCurrentDate() : date;

        ZonedDateTime today = DateUtils.parseDate(date, DateUtils.yyyyMMddDash);
        String searchStart = today.minusDays(3).format(DateUtils.yyyyMMddDash); //前三天
        String searchEnd = today.format(DateUtils.yyyyMMddDash);
        Result<FemasPunchRecordPO> result = new Result<>();
        result.setData(femasService.getPunchRecord(searchStart, searchEnd));
        return result;
    }
}
