package com.eachnow.linebot.domain.service.crawler;

import com.eachnow.linebot.common.po.javdb.ArticlePO;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import jakarta.annotation.PostConstruct;;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class JavdbCrawlerService {
    private final String JAVDB_URL = "https://javdb.com";
    public static final String TYPE_DAILY = "daily";
    public static final String TYPE_WEEKLY = "weekly";
    public static final String TYPE_MONTHLY = "monthly";
    private WebDriverFactory webDriverFactory;

    public List<ArticlePO> listDaily = new ArrayList<>();
    public List<ArticlePO> listWeekly = new ArrayList<>();
    public List<ArticlePO> listMonthly = new ArrayList<>();

    @Autowired
    public JavdbCrawlerService(WebDriverFactory webDriverFactory) {
        this.webDriverFactory = webDriverFactory;
    }

//    @PostConstruct
    private void test() {
        crawlerRankings("monthly");
    }

    public List<ArticlePO> getArticle(String type) {
        List<ArticlePO> list;
        switch (type) {
            case TYPE_WEEKLY:
                list = listWeekly;
                break;
            case TYPE_MONTHLY:
                list = listMonthly;
                break;
            default:
                list = listDaily;
        }
        if (list == null || list.size() == 0)
            crawlerRankings(TYPE_DAILY);
        return listDaily;
    }

    public String getTypeName(String type) {
        switch (type) {
            case TYPE_DAILY:
                return "日榜";
            case TYPE_WEEKLY:
                return "周榜";
            case TYPE_MONTHLY:
                return "月榜";
        }
        return "";
    }

    /**
     * 爬取排行榜
     *
     * @param type daily, weekly, monthly
     */
    public void crawlerRankings(String type) {
        if (type == null)
            type = TYPE_DAILY;
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            String url = JAVDB_URL + "/rankings/video_censored?period=daily";
            List<ArticlePO> result = new ArrayList<>(85);
            WebDriver driver = webDriverFactory.bulidDriver(url, false);
            driver.findElement(By.className("is-success")).click();
            if (TYPE_WEEKLY.equals(type) || TYPE_MONTHLY.equals(type)) {
                List<WebElement> list = driver.findElement(By.className("tabs")).findElements(By.cssSelector("a[href]"));
                for (WebElement webElement : list) {
                    if (webElement.getAttribute("href").contains(type)) {
                        webElement.click();
                        break;
                    }
                }
            }
            List<WebElement> list = driver.findElements(By.className("grid-item"));
            for (WebElement webElement : list) {
                String link = webElement.findElement(By.cssSelector("a[href]")).getAttribute("href");
                String title = webElement.findElement(By.className("video-title")).getText().replace("（ブルーレイディスク）", "");
                String author = "";
                if (title.contains(" ")) {
                    String[] titleArr = title.split(" ");
                    author = titleArr[titleArr.length - 1];
                }
                String code = webElement.findElement(By.className("uid")).getText();
                String date = webElement.findElement(By.className("meta")).getText();
                String pictureUrl = webElement.findElement(By.className("item-image")).findElement(By.cssSelector("img")).getAttribute("data-src");
                result.add(ArticlePO.builder().title(title).webUrl(link).pictureUrl(pictureUrl).code(code).author(author).date(date).build());
            }
            driver.quit();
            sw.stop();
            log.info("爬取排行榜，完成。type:{}, spend TotalTime:{}(s)", type, sw.getTotalTimeSeconds());
            switch (type) {
                case TYPE_DAILY:
                    listDaily = result;
                    break;
                case TYPE_WEEKLY:
                    listWeekly = result;
                    break;
                case TYPE_MONTHLY:
                    listMonthly = result;
                    break;
            }
        } catch (Exception e) {
            log.error("爬取排行榜，失敗! type:{}, error msg:{}", type, e.getMessage());
        }
    }

}
