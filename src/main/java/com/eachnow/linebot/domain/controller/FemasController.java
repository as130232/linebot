package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.db.po.LineUserPO;
import com.eachnow.linebot.common.po.Result;
import com.eachnow.linebot.common.po.femas.FemasPunchRecordPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.FemasService;
import com.eachnow.linebot.domain.service.line.LineUserService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.*;

@RestController
@RequestMapping("/femas")
public class FemasController {
    private final FemasService femasService;
    private final LineUserService lineUserService;

    @Autowired
    public FemasController(FemasService femasService, LineUserService lineUserService) {
        this.femasService = femasService;
        this.lineUserService = lineUserService;
    }

    @GetMapping(value = "/punch/record")
    public Result<Map<String, FemasPunchRecordPO>> punchRecord(@RequestParam(value = "date", required = false) String date) {
        date = Objects.isNull(date) ? DateUtils.getCurrentDate() : date;
        ZonedDateTime today = DateUtils.parseDate(date, DateUtils.yyyyMMddDash);
        String searchStart = today.minusDays(3).format(DateUtils.yyyyMMddDash); //前三天
        String searchEnd = today.format(DateUtils.yyyyMMddDash);
        Result<Map<String, FemasPunchRecordPO>> result = new Result<>();
        Map<String, FemasPunchRecordPO> data = new HashMap<>();
        List<LineUserPO> users = lineUserService.listUser();
        for (LineUserPO user : users) {
            String userName = user.getName();
            String femasToken = user.getFemasToken();
            if (Strings.isEmpty(userName) || Strings.isEmpty(femasToken)) {
                continue;
            }
            data.put(userName, femasService.getPunchRecordAndCache(userName, femasToken, searchStart, searchEnd));
        }
        result.setData(data);
        return result;
    }
}
