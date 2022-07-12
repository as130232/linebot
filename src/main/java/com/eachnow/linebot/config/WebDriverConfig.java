//package com.eachnow.linebot.config;
//
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.concurrent.TimeUnit;
//
//@Configuration
//public class WebDriverConfig {
//    @Value("${CHROMEDRIVER_PATH}")
//    private String CHROMEDRIVER_PATH;
//
//    @Bean(name = "chrome-driver")
//    public WebDriver getChromeDriver() {
//        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);
//        ChromeOptions options = new ChromeOptions();
////        options.setHeadless(true);  //增加效能
//        WebDriver driver = new ChromeDriver(options);
//        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
//        return driver;
//    }
//}
