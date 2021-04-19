package com.eachnow.linebot.domain.service.crawler;

import com.eachnow.linebot.domain.service.line.LineNotifySender;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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

    public List<String> crawler(String url, int pageSize) {
        List<String> result = new ArrayList<>(300);
        log.info("準備，爬取PTT版，url:{}, pageSize:{}", url, pageSize);
        WebDriver driver = webDriverFactory.bulidDriver(url, true);
        driver.findElement(By.xpath("//button[@value=\"yes\"]")).click();
        driver.navigate().refresh();
        for (int i = 0; i < pageSize; i++) {
            List<String> listPicture = crawlerOnPage(driver);    //爬取該頁素材
            result.addAll(listPicture);
            driver.findElement(By.xpath("//*[contains(text(),\"上頁\")]")).click();    //進入上頁，刷取新的素材
        }
        driver.quit();
        log.info("爬取PTT版，完成。url:{}", url);
//        //發送訊息通知
//        lineNotifyService.send(LineNotifyConstant.OWN, "爬取PTT版，完成。url:" + url);
        return result;
    }

    public List<String> crawlerOnPage(WebDriver driver) {
        List<String> result = new ArrayList<>();
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
                result.addAll(listPictureOnPage);
                driver.navigate().back();
            }
        }
        return result;
    }

    public List<String> findAllLink(WebDriver driver) {
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
                        || articleElement.getAttribute("text").contains("[問題]")
                        || articleElement.getAttribute("text").contains("[閒聊]"))
                    continue;
                availableLinks.add(link.replace("https://www.ptt.cc", ""));
            }
        }
        return availableLinks;
    }

    public List<String> findAllPicture(WebDriver driver, List<String> availableLinks) {
        List<String> result = new ArrayList<>();
        for (String link : availableLinks) {
            if (driver.findElements(By.cssSelector("a[href]")).size() > 0) {
                WebElement webElement = driver.findElement(By.cssSelector(("a[href^=\"{url}\"]").replace("{url}", link)));
                webElement.click();
                List<WebElement> listPictureElement = driver.findElements(By.cssSelector(("a[href]")));
                List<String> listPictureOnPage = listPictureElement.stream().map(e -> e.getAttribute("href"))
                        .filter(e -> e.contains("jpg")).collect(Collectors.toList());
                result.addAll(listPictureOnPage);
                driver.navigate().back();
            }
        }
        return result;
    }
}
