package com.eachnow.linebot.config;

import org.dom4j.io.SAXReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;


@Configuration
public class SAXReaderConfig {

    @Bean
    @Lazy
    protected SAXReader saxReader() {
        SAXReader saxReader = new SAXReader();
        saxReader.setEncoding("utf-8");
        return saxReader;
    }
}
