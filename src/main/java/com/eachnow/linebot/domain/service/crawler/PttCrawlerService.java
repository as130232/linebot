package com.eachnow.linebot.domain.service.crawler;

import com.eachnow.linebot.common.constant.PttConstant;
import com.eachnow.linebot.common.po.PttArticlePO;
import com.eachnow.linebot.common.util.NumberUtils;
import com.eachnow.linebot.domain.service.line.LineNotifySender;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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

    public List<PttArticlePO> crawler(String url, int pageSize, Integer sourceType) {
        List<PttArticlePO> result = new ArrayList<>(300);
        log.info("準備，爬取PTT版，url:{}, pageSize:{}", url, pageSize);
        WebDriver driver = webDriverFactory.bulidDriver(url, true);
        driver.findElement(By.xpath("//button[@value=\"yes\"]")).click();
        driver.navigate().refresh();
        for (int i = 0; i < pageSize; i++) {
            List<PttArticlePO> listPttArticle = new ArrayList<>(0);
            // 爬取該頁素材
            if (PttConstant.TYPE_ARTICLE.equals(sourceType)) {
                listPttArticle = crawlerArticleOnPage(driver);
            } else if (PttConstant.TYPE_PICTURE.equals(sourceType)) {
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
                availableLinks.add(link.replace("https://www.ptt.cc", ""));
            }
        }
        for (String link : availableLinks) {
            if (driver.findElements(By.cssSelector("a[href]")).size() > 0) {
                WebElement webElement = driver.findElement(By.cssSelector(("a[href^=\"{url}\"]").replace("{url}", link)));
                webElement.click();
                List<WebElement> listPictureElement = driver.findElements(By.cssSelector(("a[href]")));
                List<String> listPictureOnPage = listPictureElement.stream().map(e -> e.getAttribute("href"))
                        .filter(e -> e.contains("jpg") && !e.contains("https://i.imgur.com/zguYZdO.jpg") && !e.contains("https://i.imgur.com/ZfoC3ro.jpg")).collect(Collectors.toList());
                List<PttArticlePO> listPttArticle = listPictureOnPage.stream().map(picture -> PttArticlePO.builder().pictureUrl(picture).build()).collect(Collectors.toList());
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

//    @PostConstruct
    public void init() {
//        crawler(PttConstant.BEAUTY_URL, 2, PttConstant.TYPE_ARTICLE);
//        crawlerByDisp(PttConstant.SEX_DISP_URL, 2, PttConstant.TYPE_ARTICLE);
        crawlerByDisp(PttConstant.MAIN_DISP_URL, 1, PttConstant.TYPE_ARTICLE);
    }


    /***
     *
     * @param url
     * @param pageSize
     * @param sourceType {@link com.eachnow.linebot.common.constant.PttConstant}
     * @return
     */
    public List<PttArticlePO> crawlerByDisp(String url, int pageSize, Integer sourceType) {
        List<PttArticlePO> result = new ArrayList<>(300);
        log.info("準備，爬取PTT版(DISP)，url:{}, pageSize:{}", url, pageSize);
        WebDriver driver = webDriverFactory.bulidDriver(url, true);
        driver.navigate().refresh();
        for (int i = 0; i < pageSize; i++) {
            List<PttArticlePO> listPttArticle = new ArrayList<>(0);
            // 爬取該頁素材
            if (PttConstant.TYPE_ARTICLE.equals(sourceType)) {
                listPttArticle = crawlerArticleOnPageByDisp(driver);
            } else if (PttConstant.TYPE_MAIN.equals(sourceType)) {

            }
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
            String title = webElement.findElement(By.className("listTitle")).getText();
            if (title.contains("[公告]") || title.contains("刪除")) {
                continue;
            }
            String link = webElement.findElement(By.className("listTitle")).findElement(By.cssSelector("a[href]")).getAttribute("href");
            String author = webElement.findElement(By.className("user")).getText();
            String date = webElement.findElement(By.className("L12")).getText();
            WebElement popularityWebElement = webElement.findElement(By.className("R0"));
            List<WebElement> spans = popularityWebElement.findElements(By.cssSelector("span"));
            String popularityStr = spans.get(spans.size() - 1).getAttribute("title");
            Integer popularity = parsePopularity(popularityStr.replace("累積人氣: ", ""));
            result.add(PttArticlePO.builder().title(title).webUrl(link).author(author).date(date).popularity(popularity).build());
        }
        return result;
    }
}
