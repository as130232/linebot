package com.eachnow.linebot.domain.service.crawler;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class WebDriverFactory {
    @Value("${CHROMEDRIVER_PATH:D:\\chromedriver\\chromedriver.exe}")
    private String CHROMEDRIVER_PATH;
    private WebDriver driver;

    public WebDriver bulidDriver(String url, boolean headless) {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);
        log.info("CHROMEDRIVER_PATH:{}", CHROMEDRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.setHeadless(true);  //開啟無視窗模式與禁止GPU渲染，增加效能
            options.addArguments("--no-sandbox");   //關閉沙河模式
            options.addArguments("--disable-dev-shm-usage");
//            options.setPageLoadStrategy(PageLoadStrategy.NONE);
            //禁用圖片
            HashMap<String, Object> prefs = new HashMap<>();
            prefs.put("profile.default_content_settings", 2);
            options.setExperimentalOption("prefs", prefs);
            options.addArguments("blink-settings=imagesEnabled=false");
        } else {
            options.addArguments("--window-size=1920,1080");
        }
        driver = new ChromeDriver(options);
        driver.get(url);
        log.info("webdriver連線 url:{}, headless:{}", url, headless);
        return driver;
    }

}
