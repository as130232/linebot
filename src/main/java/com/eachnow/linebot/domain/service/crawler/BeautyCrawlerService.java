package com.eachnow.linebot.domain.service.crawler;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 爬取資源服務
 */
@Slf4j
@Component
public class BeautyCrawlerService {
    @Value("${CHROMEDRIVER_PATH:D:\\chromedriver\\chromedriver.exe}")
    private String CHROMEDRIVER_PATH;
    private WebDriver driver;
    public final Integer MAX_SIZE = 500;
    public List<String> listPicture = new ArrayList<>(MAX_SIZE);
    private final ThreadPoolExecutor beautyCrawlerExecutor;

    @Autowired
    public BeautyCrawlerService(@Qualifier("beauty-crawler-executor") ThreadPoolExecutor beautyCrawlerExecutor) {
        this.beautyCrawlerExecutor = beautyCrawlerExecutor;
    }

    //    @PostConstruct
    public void init() {
        log.info("清空圖庫，並重新爬取表特版。");
        listPicture =  new ArrayList<>(MAX_SIZE);
        crawler(2);
    }

    public void crawler(int pageSize) {
        CompletableFuture.runAsync(() -> {
            log.info("準備，爬取表特版，pageSize:{}", pageSize);
            bulidDriver();
            driver.navigate().refresh();
            for (int i = 0; i < pageSize; i++) {
                crawlerOnPage();    //爬取該頁素材
                driver.findElement(By.xpath("//*[contains(text(),\"上頁\")]")).click();    //進入上頁，刷取新的素材
            }
            driver.quit();
            log.info("爬取表特版，完成。");
        }, beautyCrawlerExecutor).exceptionally(e -> {
                    log.error("爬取表特版，失敗! error msg:{}", e.getMessage(), e);
                    return null;
                }
        );
    }

    private void bulidDriver() {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);
        log.info("CHROMEDRIVER_PATH:{}", CHROMEDRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);  //無視窗模式，增加效能
//        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        String url = "https://www.ptt.cc/bbs/Beauty/index.html";
        driver.get(url);
        log.info("連線url:{}", url);
        driver.findElement(By.xpath("//button[@value=\"yes\"]")).click();   //進入表特版
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
                setPicture(listPictureOnPage);
                driver.navigate().back();
            }
        }
    }

    private void setPicture(List<String> listPictureOnPage) {
        listPicture.addAll(listPictureOnPage);
        if (listPicture.size() > MAX_SIZE) {
            int i = 0;
            while (listPicture.size() == MAX_SIZE) {
                listPicture.remove(i);
                i++;
            }
        }
    }

    public String randomPicture() {
        int item = new Random().nextInt(listPicture.size());
        return listPicture.get(item);
    }
}
