package com.eachnow.linebot.domain.service.crawler;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WebDriverFactory {
    @Value("${CHROMEDRIVER_PATH:D:\\chromedriver\\chromedriver.exe}")
    private String CHROMEDRIVER_PATH;
    private WebDriver driver;

    public WebDriver bulidDriver(String url) {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);
        log.info("CHROMEDRIVER_PATH:{}", CHROMEDRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);  //無視窗模式，增加效能
//        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        driver.get(url);
        log.info("webdriver連線url: {}", url);
        driver.findElement(By.xpath("//button[@value=\"yes\"]")).click();
        return driver;
    }


}
