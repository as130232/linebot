package com.eachnow.linebot.config;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {
    /**
     * 預設 RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    @Bean("proxy-resttemplate")
    public RestTemplate restTemplateBet188(@Qualifier("proxy-factory") ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    @Bean("proxy-factory")
    public ClientHttpRequestFactory simpleClientHttpRequestFactoryBet188() {
        PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(30, TimeUnit.SECONDS);
        //最大连接数
        pollingConnectionManager.setMaxTotal(1000);
        //单路由的并发数
        pollingConnectionManager.setDefaultMaxPerRoute(1000);
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
//        httpClientBuilder.setProxy(new HttpHost("newproxy-java.innotech.me",3129));
        httpClientBuilder.setConnectionManager(pollingConnectionManager);
        // disable expect continue 不然188會回417
        RequestConfig defaultRequestConfig = RequestConfig.custom().setExpectContinueEnabled(false).build();
        httpClientBuilder.setDefaultRequestConfig(defaultRequestConfig);
        // 重试次数2次，并开启
//        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(2, true));
        // 保持长连接配置，需要在头添加Keep-Alive
        httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        HttpClient httpClient = httpClientBuilder.build();
        // httpClient连接底层配置clientHttpRequestFactory
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        // 连接超时时长配置
        clientHttpRequestFactory.setConnectTimeout(5000);
        // 数据读取超时时长配置
        clientHttpRequestFactory.setReadTimeout(5000);
        // 连接不够用的等待时间，不宜过长，必须设置，比如连接不够用时，时间过长将是灾难性的
        clientHttpRequestFactory.setConnectionRequestTimeout(200);
        // 缓冲请求数据，默认值是true。通过POST或者PUT大量发送数据时，建议将此属性更改为false，以免耗尽内存。
        clientHttpRequestFactory.setBufferRequestBody(false);
        return clientHttpRequestFactory;
    }


}
