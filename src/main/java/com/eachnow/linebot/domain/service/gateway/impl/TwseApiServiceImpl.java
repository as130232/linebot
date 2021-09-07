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
        } else if (value.contains(","))
            value = value.replace(",", "");
        return value;
    }

    private Double toDouble(String value) {
        if (value.contains(","))
            value = value.replace(",", "");
        return Double.valueOf(value);
    }

    @Override
    public List<PricePO> getStockPrice() {
        try {
            String url = TWSE_URL + "/exchangeReport/STOCK_DAY_AVG_ALL";
            ResponseEntity<TwseDataPO> responseEntity = restTemplate.getForEntity(url, TwseDataPO.class);
            TwseDataPO twseDataPO = responseEntity.getBody();
            if ("很抱歉，沒有符合條件的資料!".equals(twseDataPO.getStat()))
                return new ArrayList<>(0);
            List<PricePO> result = twseDataPO.getData().stream().map(list -> PricePO.builder().code(list.get(0)).name(list.get(1))
                    .price(Double.valueOf(parseValue(list.get(2)))).avePrice(Double.valueOf(parseValue(list.get(3)))).build()).collect(Collectors.toList());
            return result;
        } catch (Exception e) {
            log.error("呼叫取得證交所-取得當日所有個股股價，失敗! error msg:{}", e.getMessage());
        }
        return new ArrayList<>(0);
    }

    @Override
    public TwseStockInfoDataPO getStockInfo(String code) {
        try {
            String stockId = "tse_{code}.tw".replace("{code}", code);
            String url = String.format("https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_%s.tw&json=1&delay=0", stockId);
            ResponseEntity<TwseStockInfoDataPO> responseEntity = restTemplate.getForEntity(url, TwseStockInfoDataPO.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("呼叫取得證交所-個股當日即時狀況，失敗! stockId:{}, error msg:{}", code, e.getMessage());
        }
        return null;
    }

    @Override
    public IndexPO getDailyTradingOfTaiwanIndex(String date) {
        LocalDate localDate = LocalDate.parse(date, DateUtils.yyyyMMdd);
        try {
            String url = TWSE_URL + "/exchangeReport/FMTQIK?response=json&date=" + date;
            ResponseEntity<TwseDataPO> responseEntity = restTemplate.getForEntity(url, TwseDataPO.class);
            TwseDataPO twseDataPO = responseEntity.getBody();
            if ("很抱歉，沒有符合條件的資料!".equals(twseDataPO.getStat()))
                return null;
            for (int i = 0; i < twseDataPO.getData().size(); i++) {
                List<String> listData = twseDataPO.getData().get(i);
                String dataDate = listData.get(0);    //日期 110/05/03
                int month = Integer.parseInt(dataDate.split("/")[1]);
                int day = Integer.parseInt(dataDate.split("/")[2]);
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
            if ("很抱歉，沒有符合條件的資料!".equals(twseDataPO.getStat()))
                return new ArrayList<>(0);
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
        return new ArrayList<>(0);
    }

    @Override
    public List<RatioAndDividendYieldPO> getRatioAndDividendYield(String date) {
        try {
            String url = TWSE_URL + "/exchangeReport/BWIBBU_d?response=json&selectType=ALL&date=" + date;
            ResponseEntity<TwseDataPO> responseEntity = restTemplate.getForEntity(url, TwseDataPO.class);
            TwseDataPO twseDataPO = responseEntity.getBody();
            if ("很抱歉，沒有符合條件的資料!".equals(twseDataPO.getStat()))
                return new ArrayList<>(0);
            List<RatioAndDividendYieldPO> result = twseDataPO.getData().stream().map(list -> {
                PricePO pricePO = this.getPrice(list.get(0));
                return RatioAndDividendYieldPO.builder().code(list.get(0)).name(list.get(1)).dividendYield(Double.valueOf(parseValue(list.get(2))))
                        .peRatio(Double.valueOf(parseValue(list.get(4)))).pbRatio(Double.valueOf(parseValue(list.get(5)))).price(pricePO.getPrice()).avePrice(pricePO.getAvePrice()).build();
            }).collect(Collectors.toList());
            return result;
        } catch (Exception e) {
            log.error("呼叫取得證交所-取得個股本益比、股價淨值比及殖利率，失敗! date:{}, error msg:{}", date, e.getMessage());
        }
        return new ArrayList<>(0);
    }

    @Override
    public List<RatioAndDividendYieldPO> getRatioAndDividendYieldOnMonth(String code) {
        try {
            String url = TWSE_URL + "/exchangeReport/BWIBBU?response=json&stockNo" + code;
            ResponseEntity<TwseDataPO> responseEntity = restTemplate.getForEntity(url, TwseDataPO.class);
            TwseDataPO twseDataPO = responseEntity.getBody();
            if ("很抱歉，沒有符合條件的資料!".equals(twseDataPO.getStat()))
                return new ArrayList<>(0);
            List<RatioAndDividendYieldPO> result = twseDataPO.getData().stream().map(list -> {
                PricePO pricePO = this.getPrice(list.get(0));
                return RatioAndDividendYieldPO.builder().code(code).name(pricePO.getName()).price(pricePO.getPrice()).avePrice(pricePO.getAvePrice())
                        .date(list.get(0)).dividendYield(Double.valueOf(parseValue(list.get(1))))
                        .peRatio(Double.valueOf(parseValue(list.get(3)))).pbRatio(Double.valueOf(parseValue(list.get(4)))).build();
            }).collect(Collectors.toList());
            return result;
        } catch (Exception e) {
            log.error("呼叫取得證交所-取得個股該月份本益比、殖利率及股價淨值比，失敗! code:{}, error msg:{}", code, e.getMessage());
        }
        return new ArrayList<>(0);
    }

    @Override
    public TradeValueInfoPO getTradingOfForeignAndInvestors(String type, String date) {
        try {
            String url = TWSE_URL + "/fund/BFI82U?response=json&type=" + type;
            if (date != null)
                url += "&{type}Date={date}".replace("{type}", type).replace("{date}", date);
            ResponseEntity<TwseDataPO> responseEntity = restTemplate.getForEntity(url, TwseDataPO.class);
            TwseDataPO twseDataPO = responseEntity.getBody();
            if ("很抱歉，沒有符合條件的資料!".equals(twseDataPO.getStat()))
                return null;
            List<TradeValuePO> listTradeValues = twseDataPO.getData().stream()
                    .filter(list -> !"外資自營商".equals(list.get(0)))
                    .map(list -> TradeValuePO.builder().item(list.get(0))
                            .totalBuy(toDouble(list.get(1)))
                            .totalSell(toDouble(list.get(2)))
                            .difference(toDouble(list.get(3))).build()).collect(Collectors.toList());
            return TradeValueInfoPO.builder().title(twseDataPO.getTitle()).tradeValues(listTradeValues).build();
        } catch (Exception e) {
            log.error("呼叫取得證交所-三大法人買賣金額統計表，失敗! type:{}, error msg:{}", type, e.getMessage());
        }
        return null;
    }

    @Override
    public List<TradeValuePO> getMarginTradingAndShortSelling(String date) {
        try {
            String url = TWSE_URL + "/exchangeReport/MI_MARGN?response=json&selectType=MS&date=" + date;
            ResponseEntity<TwseDataCreditPO> responseEntity = restTemplate.getForEntity(url, TwseDataCreditPO.class);
            TwseDataCreditPO twseDataPO = responseEntity.getBody();
            if ("很抱歉，沒有符合條件的資料!".equals(twseDataPO.getStat()))
                return new ArrayList<>(0);
            List<TradeValuePO> result = twseDataPO.getCreditList().stream().map(list -> {
                double unit = 1l;
                //單位換算成元
                if("融資金額(仟元)".equals(list.get(0)))
                    unit = 1000l;
                return TradeValuePO.builder().item(list.get(0))
                        .totalBuy(toDouble(list.get(1)) * unit)
                        .totalSell(toDouble(list.get(2)) * unit)
                        .balanceOfPreDay(toDouble(list.get(4)) * unit)
                        .balance(toDouble(list.get(5)) * unit)
                        .difference((toDouble(list.get(5)) - toDouble(list.get(4))) * unit).build();
            }).collect(Collectors.toList());
            return result;
        } catch (Exception e) {
            log.error("呼叫取得證交所-融資融券餘額，失敗! date:{}, error msg:{}", date, e.getMessage());
        }
        return new ArrayList<>(0);
    }


}
