package com.eachnow.linebot.domain.service.crawler;

import com.eachnow.linebot.common.constant.PttEnum;
import com.eachnow.linebot.common.po.PttArticlePO;
import com.eachnow.linebot.common.po.twse.IndexPO;
import com.eachnow.linebot.common.util.NumberUtils;
import com.eachnow.linebot.domain.service.line.LineNotifySender;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PttCrawlerService {
    private WebDriverFactory webDriverFactory;
    private LineNotifySender lineNotifySender;

    @Autowired
    public PttCrawlerService(WebDriverFactory webDriverFactory,
                             LineNotifySender lineNotifySender) {
        this.webDriverFactory = webDriverFactory;
        this.lineNotifySender = lineNotifySender;
    }

    /**
     * 爬取PTT資源(BBS)
     *
     * @param pttEnum    PTT網址列舉
     * @param pageSize   爬取頁數
     * @param sourceType {@link com.eachnow.linebot.common.constant.PttEnum}
     */
    public List<PttArticlePO> crawler(PttEnum pttEnum, int pageSize, Integer sourceType) {
        List<PttArticlePO> result = new ArrayList<>(300);
        String url = pttEnum.getUrl(pttEnum);
        log.info("準備，爬取PTT版，url:{}, pageSize:{}", url, pageSize);
        WebDriver driver = webDriverFactory.bulidDriver(url, true);
        driver.findElement(By.xpath("//button[@value=\"yes\"]")).click();
        driver.navigate().refresh();
        for (int i = 0; i < pageSize; i++) {
            List<PttArticlePO> listPttArticle = new ArrayList<>(0);
            // 爬取該頁素材
            if (PttEnum.TYPE_ARTICLE.equals(sourceType)) {
                listPttArticle = crawlerArticleOnPage(driver);
            } else if (PttEnum.TYPE_PICTURE.equals(sourceType)) {
                listPttArticle = crawlerPictureOnPage(driver);
            }
            result.addAll(listPttArticle);
            driver.findElement(By.xpath("//*[contains(text(),\"上頁\")]")).click();    //進入上頁，刷取新的素材
        }
        driver.quit();
        log.info("爬取PTT版，完成。url:{}", url);
        //發送訊息通知
//        lineNotifyService.send(LineNotifyConstant.OWN, "爬取PTT版，完成。url:" + url);
        return result;
    }

    /**
     * 爬取該網址文章列表與文章裡所有圖片資源
     */
    public List<PttArticlePO> crawlerPictureOnPage(WebDriver driver) {
        List<PttArticlePO> result = new ArrayList<>();
        //取得該頁所有文章連結
        List<String> availableLinks = new ArrayList<>();
        List<WebElement> listLinkOnPage = driver.findElements(By.className("title"));  //該頁所有連結
        for (WebElement webElement : listLinkOnPage) {
            if (webElement.findElements(By.cssSelector("a[href]")).size() > 0) {
                WebElement articleElement = webElement.findElement(By.cssSelector("a[href]"));
                String link = articleElement.getAttribute("href");
                //過濾連結
                if (articleElement.getAttribute("text").contains("[公告]")
                        || articleElement.getAttribute("text").contains("[帥哥]")
                        || articleElement.getAttribute("text").contains("肉特")
                        || articleElement.getAttribute("text").contains("[申請]")
                        || articleElement.getAttribute("text").contains("[問題]")
                        || articleElement.getAttribute("text").contains("[閒聊]"))
                    continue;
                availableLinks.add(link);
            }
        }
        for (String link : availableLinks) {
            if (driver.findElements(By.cssSelector("a[href]")).size() > 0) {
                String replaceLink = link.replace("https://www.ptt.cc", "");
                WebElement webElement = driver.findElement(By.cssSelector(("a[href^=\"{url}\"]").replace("{url}", replaceLink)));
                webElement.click();
                List<WebElement> listPictureElement = driver.findElements(By.cssSelector(("a[href]")));
                List<String> listPictureOnPage = listPictureElement.stream().map(e -> e.getAttribute("href"))
                        .filter(e -> e.contains("jpg") && !e.contains("https://i.imgur.com/zguYZdO.jpg") && !e.contains("https://i.imgur.com/ZfoC3ro.jpg")).collect(Collectors.toList());
                List<PttArticlePO> listPttArticle = listPictureOnPage.stream().map(picture -> PttArticlePO.builder()
                        .pictureUrl(picture).webUrl(link).build()).collect(Collectors.toList());
                result.addAll(listPttArticle);
                driver.navigate().back();
            }
        }
        return result;
    }

    /**
     * 爬取該網址文章列表所有文章資源
     */
    public List<PttArticlePO> crawlerArticleOnPage(WebDriver driver) {
        List<PttArticlePO> result = new ArrayList<>();
        List<WebElement> listLinkOnPage = driver.findElements(By.className("r-ent"));  //該頁所有連結
        for (WebElement webElement : listLinkOnPage) {
            try {
                WebElement articleElement = webElement.findElement(By.className("title")).findElement(By.cssSelector("a[href]"));
                String title = articleElement.getText();
                if (title.contains("[公告]") || title.contains("刪除")) {
                    continue;
                }
                String link = articleElement.getAttribute("href");
                //爆讚數
                WebElement metaElement = webElement.findElement(By.className("meta"));
                String author = metaElement.findElement(By.className("author")).getText();
                String date = metaElement.findElement(By.className("date")).getText();
                String popularityStr = webElement.findElement(By.className("nrec")).getText();
                Integer popularity = parsePopularity(popularityStr);
                result.add(PttArticlePO.builder().title(title).webUrl(link).author(author).date(date).popularity(popularity).build());
            } catch (Exception e) {
                log.error("crawlerArticleOnPage failed! error msg:{}", e.getMessage());
            }
        }
        return result;
    }

    private Integer parsePopularity(String popularityStr) {
        if (NumberUtils.isNumber(popularityStr)) {
            return Integer.valueOf(popularityStr);
        }
        Integer popularity = 0;
        if (popularityStr != null && popularityStr.toUpperCase(Locale.ROOT).contains("X")) {
            popularity = -100 * (Integer.valueOf(popularityStr.replace("X", "")));
        } else if (popularityStr != null && popularityStr.contains("爆")) {
            popularity = 1000;
        }
        return popularity;
    }

    /**
     * 爬取PTT資源(DISP)
     *
     * @param pttEnum  PTT網址列舉
     * @param pageSize 爬取頁數
     */
    public List<PttArticlePO> crawlerByDisp(PttEnum pttEnum, int pageSize) {
        List<PttArticlePO> result = new ArrayList<>(300);
        String url = pttEnum.getUrlByDisp(pttEnum);
        log.info("準備，爬取PTT版(DISP)，url:{}, pageSize:{}", url, pageSize);
        WebDriver driver = webDriverFactory.bulidDriver(url, true);
        driver.navigate().refresh();
        for (int i = 0; i < pageSize; i++) {
            List<PttArticlePO> listPttArticle = crawlerArticleOnPageByDisp(driver);
            result.addAll(listPttArticle);
            driver.findElement(By.xpath("//*[contains(text(),\"上一頁\")]")).click();    //進入上頁，刷取新的素材
        }
        driver.quit();
        log.info("爬取PTT版(DISP)，完成。url:{}", url);
        //發送訊息通知
//        lineNotifyService.send(LineNotifyConstant.OWN, "爬取PTT版，完成。url:" + url);
        return result;
    }

    /**
     * 爬取該網址文章列表所有文章資源
     */
    public List<PttArticlePO> crawlerArticleOnPageByDisp(WebDriver driver) {
        List<PttArticlePO> result = new ArrayList<>();
        List<WebElement> listLinkOnPage = driver.findElements(By.className("row2"));
        for (WebElement webElement : listLinkOnPage) {
            try {
                String title = webElement.findElement(By.className("listTitle")).getText();
                if (title.contains("[公告]") || title.contains("刪除")) {
                    continue;
                }
                String link = webElement.findElement(By.className("listTitle")).findElement(By.cssSelector("a[href]")).getAttribute("href");
                String author = webElement.findElement(By.className("user")).getText();
                String date = webElement.findElement(By.className("L12")).getText();
                WebElement popularityWebElement = webElement.findElement(By.className("R0"));
                List<WebElement> spans = popularityWebElement.findElements(By.cssSelector("span"));
                Integer popularity = 0;
                if (spans.size() > 0) {
                    String popularityStr = spans.get(spans.size() - 1).getAttribute("title");
                    popularity = parsePopularity(popularityStr.replace("累積人氣: ", ""));
                }
                result.add(PttArticlePO.builder().title(title).webUrl(link).author(author).date(date).popularity(popularity).build());
            } catch (Exception e) {
                log.error("crawlerArticleOnPageByDisp failed! error msg:{}", e.getMessage());
            }
        }
        return result;
    }

//    @PostConstruct
    public void init() {
        StopWatch sw1 = new StopWatch();
        sw1.start();
        List<PttArticlePO> list1 = crawler(PttEnum.GOSSIPING, 1, PttEnum.TYPE_ARTICLE);
        sw1.stop();
        StopWatch sw2 = new StopWatch();
        sw2.start();
        List<PttArticlePO> list2 = crawlerByDisp(PttEnum.GOSSIPING, 1);
        sw2.stop();
        log.info("list1:{}, sw1:{}, list2:{}, sw2:{}", list1.size(), sw1.getTotalTimeMillis(), list2.size(), sw2.getTotalTimeMillis());
        System.out.println("a");
        List<PttArticlePO> sortList2 = list2.stream().sorted(Comparator.comparing(PttArticlePO::getPopularity).reversed()).collect(Collectors.toList());
    }
}
