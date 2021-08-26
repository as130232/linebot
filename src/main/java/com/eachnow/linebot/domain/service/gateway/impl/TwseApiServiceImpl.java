package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.po.twse.*;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.gateway.TwseApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TwseApiServiceImpl implements TwseApiService {
    private String TWSE_URL = "https://www.twse.com.tw";
    private RestTemplate restTemplate;

    private Map<String, PricePO> priceMap = new HashMap<>();

    @Autowired
    public TwseApiServiceImpl(@Qualifier("https-resttemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        initPriceMap(); //啟動先拉取最新股價
    }

    @Override
    public void initPriceMap() {
        priceMap = this.getStockPrice().stream().collect(Collectors.toMap(PricePO::getCode, Function.identity()));
        System.out.println();
    }

    @Override
    public PricePO getPrice(String code) {
        PricePO pricePO = this.priceMap.get(code);
        if (pricePO == null) {
            initPriceMap();
            pricePO = this.priceMap.get(code);
        }
        return pricePO;
    }

//    @PostConstruct
//    private void test() {
//        IndexPO indexPO = this.getDailyTradingOfTaiwanIndex("20210507");
//        System.out.println(indexPO);
//        List<IndexPO> list = this.getDailyTradeSummaryOfAllIndex("20210510");
//        System.out.println(list);
//        List<RatioAndDividendYieldPO>  list = this.getRatioAndDividendYield("20210824");
//        System.out.println(list);
//    }

    private String parseValue(String value) {
        if (value.contains("-")) {
            return "-1";
        }
        return value;
    }

    @Override
    public List<PricePO> getStockPrice() {
        try {
            String url = TWSE_URL + "/exchangeReport/STOCK_DAY_AVG_ALL";
            ResponseEntity<TwseDataPO> responseEntity = restTemplate.getForEntity(url, TwseDataPO.class);
            TwseDataPO twseDataPO = responseEntity.getBody();
            List<PricePO> result = twseDataPO.getData().stream().map(list -> {
                return PricePO.builder().code(list.get(0)).name(list.get(1)).price(parseValue(list.get(2))).avePrice(parseValue(list.get(3))).build();
            }).collect(Collectors.toList());
            return result;
        } catch (Exception e) {
            log.error("呼叫取得證交所-取得當日所有個股股價，失敗! error msg:{}", e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public IndexPO getDailyTradingOfTaiwanIndex(String date) {
        LocalDate localDate = LocalDate.parse(date, DateUtils.yyyyMMdd);
        try {
            String url = TWSE_URL + "/exchangeReport/FMTQIK?response=json&date=" + date;
            ResponseEntity<TwseDataPO> responseEntity = restTemplate.getForEntity(url, TwseDataPO.class);
            TwseDataPO twseDataPO = responseEntity.getBody();
            for (int i = 0; i < twseDataPO.getData().size(); i++) {
                List<String> listData = twseDataPO.getData().get(i);
                String dataDate = listData.get(0);    //日期 110/05/03
                Integer month = Integer.valueOf(dataDate.split("/")[1]);
                Integer day = Integer.valueOf(dataDate.split("/")[2]);
                if (localDate.getMonth().getValue() == month && localDate.getDayOfMonth() == day) {
                    return IndexPO.builder().tradeVolume(listData.get(1)).tradeValue(listData.get(2))
                            .transaction(listData.get(3)).taiex(listData.get(4)).change(Float.valueOf(listData.get(5)))
                            .name("台股大盤成交資訊").date(localDate.format(DateUtils.yyyyMMddDash)).build();
                }
            }
        } catch (Exception e) {
            log.error("呼叫取得證交所-各類指數日成交量值，失敗! date:{}, error msg:{}", date, e.getMessage());
        }
        return null;
    }

    @Override
    public List<IndexPO> getDailyTradeSummaryOfAllIndex(String date) {
        try {
            String url = TWSE_URL + "/exchangeReport/BFIAMU?response=json&date=" + date;
            ResponseEntity<TwseDataPO> responseEntity = restTemplate.getForEntity(url, TwseDataPO.class);
            TwseDataPO twseDataPO = responseEntity.getBody();
            List<IndexPO> result = new ArrayList<>(twseDataPO.getData().size());
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
        return new ArrayList<>();
    }

    @Override
    public List<RatioAndDividendYieldPO> getRatioAndDividendYield(String date) {
        try {
            String url = TWSE_URL + "/exchangeReport/BWIBBU_d?response=json&selectType=ALL&date=" + date;
            ResponseEntity<TwseDataPO> responseEntity = restTemplate.getForEntity(url, TwseDataPO.class);
            TwseDataPO twseDataPO = responseEntity.getBody();
            List<RatioAndDividendYieldPO> result = twseDataPO.getData().stream().map(list -> {
                PricePO pricePO = this.getPrice(list.get(0));
                return RatioAndDividendYieldPO.builder().code(list.get(0)).name(list.get(1)).dividendYield(parseValue(list.get(2)))
                        .peRatio(parseValue(list.get(4))).pbRatio(parseValue(list.get(5))).price(pricePO.getPrice()).avgPrice(pricePO.getAvePrice()).build();
            }).collect(Collectors.toList());
            return result;
        } catch (Exception e) {
            log.error("呼叫取得證交所-取得個股本益比、股價淨值比及殖利率，失敗! date:{}, error msg:{}", date, e.getMessage());
        }
        return new ArrayList<>();
    }

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

}
