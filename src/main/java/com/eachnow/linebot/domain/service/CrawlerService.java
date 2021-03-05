package com.eachnow.linebot.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 爬取資源服務
 */
@Slf4j
@Component
public class CrawlerService {
    private WebDriver driver;
    private Set<String> listPicture = new HashSet<>(1000);

    @Autowired
    public CrawlerService() {
    }

    private void init() {
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);  //增加效能
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        String url = "https://www.ptt.cc/bbs/Beauty/index.html";
        driver.get(url);
        driver.findElement(By.xpath("//button[@value=\"yes\"]")).click();   //進入表特版
    }

//    @PostConstruct
    private void crawler() {
        init();
        int pageSize = 2;
        for (int i = 0; i < pageSize; i++) {
//            driver.navigate().refresh();
            crawlerOnPage();    //爬取該頁素材
            driver.findElement(By.xpath("//*[contains(text(),\"上頁\")]")).click();    //進入上頁，刷取新的素材
        }
        driver.quit();

    }

    private void crawlerOnPage() {
        //取得該頁所有文章連結
        List<String> availablelinks = new ArrayList<>();
        List<WebElement> listLinkOnPage = driver.findElements(By.className("title"));  //該頁所有連結
        for (WebElement webElement : listLinkOnPage) {
            if (webElement.findElements(By.cssSelector("a[href]")).size() > 0) {
                WebElement articleElement = webElement.findElement(By.cssSelector("a[href]"));
                String link = articleElement.getAttribute("href");
                if (articleElement.getAttribute("text").contains("[公告]") || articleElement.getAttribute("text").contains("[帥哥]"))  //過濾連結
                    continue;
                availablelinks.add(link.replace("https://www.ptt.cc", ""));
            }
        }
        for (String link : availablelinks) {
            if (driver.findElements(By.cssSelector("a[href]")).size() > 0) {
                WebElement webElement = driver.findElement(By.cssSelector(("a[href^=\"{url}\"]").replace("{url}", link)));
                webElement.click();
                List<WebElement> listPictureElement = driver.findElements(By.cssSelector(("a[href]")));
                List<String> listPictureOnPage = listPictureElement.stream().map(e -> e.getAttribute("href"))
                        .filter(e -> e.contains("jpg")).collect(Collectors.toList());
                listPicture.addAll(listPictureOnPage);
                driver.navigate().back();
            }
        }
    }


    private void test() {
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);  //增加效能
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        String url = "https://www.ptt.cc/bbs/Beauty/index.html";
        driver.get(url);
        driver.findElement(By.xpath("//button[@value=\"yes\"]")).click();          //進入表特版
//        driver.findElement(By.xpath("//*[contains(text(),\"上頁\")]")).click();    //進入第N頁

        //取得該頁所有文章連結
        List<String> availablelinks = new ArrayList<>();
        List<WebElement> listLinkOnPage = driver.findElements(By.className("title"));  //該頁所有連結
        for (WebElement webElement : listLinkOnPage) {
            if (webElement.findElements(By.cssSelector("a[href]")).size() > 0) {
                WebElement articleElement = webElement.findElement(By.cssSelector("a[href]"));
                String link = articleElement.getAttribute("href");
                if (articleElement.getAttribute("text").contains("[公告]") || articleElement.getAttribute("text").contains("[帥哥]"))  //過濾連結
                    continue;
                availablelinks.add(link.replace("https://www.ptt.cc", ""));
            }
        }

        List<String> allPictureLinks = new ArrayList<>();
        //隨機進入一頁
        for (String link : availablelinks) {
            if (driver.findElements(By.cssSelector("a[href]")).size() > 0) {
                WebElement webElement = driver.findElement(By.cssSelector(("a[href^=\"{url}\"]").replace("{url}", link)));
                webElement.click();
                List<WebElement> listPictureElement = driver.findElements(By.cssSelector(("a[href]")));
                List<String> listPicture = listPictureElement.stream().map(e -> e.getAttribute("href"))
                        .filter(e -> e.contains("jpg")).collect(Collectors.toList());
                allPictureLinks.addAll(listPicture);
                driver.navigate().back();
            }
        }
        driver.quit();
        System.out.println(allPictureLinks);


    }

}
