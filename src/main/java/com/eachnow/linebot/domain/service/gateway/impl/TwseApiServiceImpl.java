package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.po.twse.IndexPO;
import com.eachnow.linebot.common.po.twse.TwseDataPO;
import com.eachnow.linebot.common.po.twse.TwseStockInfoDataPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.gateway.TwseApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TwseApiServiceImpl implements TwseApiService {
    private String TWSE_URL = "https://www.twse.com.tw/";
    private RestTemplate restTemplate;

    @Autowired
    public TwseApiServiceImpl(@Qualifier("https-resttemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

//    @PostConstruct
//    private void test() {
//        IndexPO indexPO = this.getDailyTradingOfTaiwanIndex("20210507");
//        System.out.println(indexPO);
//        List<IndexPO> list = this.getDailyTradeSummaryOfAllIndex("20210510");
//        System.out.println(list);
//    }

    public TwseStockInfoDataPO getStockInfo(String stockId) {
        try {
            String url = String.format("https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_%s.tw&json=1&delay=0", stockId);
            ResponseEntity<TwseStockInfoDataPO> responseEntity = restTemplate.getForEntity(url, TwseStockInfoDataPO.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("呼叫取得證交所-個股當日即時狀況，失敗! stockId:{}, error msg:{}", stockId, e.getMessage());
        }
        return null;
    }

    @Override
    public IndexPO getDailyTradingOfTaiwanIndex(String date) {
        String title = "台股大盤成交資訊";
        LocalDate localDate = LocalDate.parse(date, DateUtils.yyyyMMdd);
        IndexPO indexPO = IndexPO.builder().name(title).date(localDate.format(DateUtils.yyyyMMddDash)).build();
        try {
            String url = TWSE_URL + "exchangeReport/FMTQIK?response=json&date=" + date;
            ResponseEntity<TwseDataPO> responseEntity = restTemplate.getForEntity(url, TwseDataPO.class);
            TwseDataPO twseDataPO = responseEntity.getBody();
            for (int i = 0; i < twseDataPO.getData().size(); i++) {
                List<String> listData = twseDataPO.getData().get(i);
                String dataDate = listData.get(0);    //日期 110/05/03
                Integer month = Integer.valueOf(dataDate.split("/")[1]);
                Integer day = Integer.valueOf(dataDate.split("/")[2]);
                if (localDate.getMonth().getValue() == month && localDate.getDayOfMonth() == day) {
                    indexPO = IndexPO.builder().tradeVolume(listData.get(1)).tradeValue(listData.get(2)).transaction(listData.get(3))
                            .taiex(listData.get(4)).change(Float.valueOf(listData.get(5))).name(title).date(localDate.format(DateUtils.yyyyMMddDash)).build();
                    return indexPO;
                }
            }
        } catch (Exception e) {
            log.error("呼叫取得證交所-各類指數日成交量值，失敗! date:{}, error msg:{}", date, e.getMessage());
        }
        return null;
    }

    @Override
    public List<IndexPO> getDailyTradeSummaryOfAllIndex(String date) {
        List<IndexPO> result = new ArrayList<>();
        try {
            String url = TWSE_URL + "exchangeReport/BFIAMU?response=json&date=" + date;
            ResponseEntity<TwseDataPO> responseEntity = restTemplate.getForEntity(url, TwseDataPO.class);
            TwseDataPO twseDataPO = responseEntity.getBody();
            twseDataPO.getData().forEach(listData -> {
                IndexPO indexPO = IndexPO.builder().name(listData.get(0)).tradeVolume(listData.get(1)).tradeValue(listData.get(2)).transaction(listData.get(3))
                        .change(Float.valueOf(listData.get(4))).date(DateUtils.parseDate(twseDataPO.getDate(), DateUtils.yyyyMMdd, DateUtils.yyyyMMddDash)).build();
                result.add(indexPO);
            });
            //排序，漲幅高、交易量大的在前
            return result.stream().sorted(Comparator.comparing(IndexPO::getChange).reversed()).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("呼叫取得證交所-各類指數日成交量值，失敗! date:{}, error msg:{}", date, e.getMessage());
        }
        return result;
    }
}
