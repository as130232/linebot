package com.eachnow.linebot.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Slf4j
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

    @Bean("converter-resttemplate")
    public RestTemplate converterRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }

    /**
     * 若調用沒有證書的https會出現"PKIX path building failed"錯誤
     */
    @Bean("https-resttemplate")
    public RestTemplate httpsRestTemplate(@Qualifier("https-request-factory") ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

//    @Bean("proxy-resttemplate")
//    public RestTemplate proxyRestTemplate(@Qualifier("proxy-factory") ClientHttpRequestFactory factory) {
//        RestTemplate restTemplate = new RestTemplate(factory);
//        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
//        return restTemplate;
//    }


//    @Bean("proxy-factory")
//    public ClientHttpRequestFactory simpleClientHttpRequestFactoryBet188() {
//        PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(30, TimeUnit.SECONDS);
//        //最大连接数
//        pollingConnectionManager.setMaxTotal(1000);
//        //单路由的并发数
//        pollingConnectionManager.setDefaultMaxPerRoute(1000);
//        HttpClientBuilder httpClientBuilder = HttpClients.custom();
//        httpClientBuilder.setConnectionManager(pollingConnectionManager);
//        // disable expect continue 不然188會回417
//        RequestConfig defaultRequestConfig = RequestConfig.custom().setExpectContinueEnabled(false).build();
//        httpClientBuilder.setDefaultRequestConfig(defaultRequestConfig);
//        // 重试次数2次，并开启
////        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(2, true));
//        // 保持长连接配置，需要在头添加Keep-Alive
//        httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
//        HttpClient httpClient = httpClientBuilder.build();
//        // httpClient连接底层配置clientHttpRequestFactory
//        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
//        // 连接超时时长配置
//        clientHttpRequestFactory.setConnectTimeout(5000);
//        // 数据读取超时时长配置
//        clientHttpRequestFactory.setReadTimeout(5000);
//        // 连接不够用的等待时间，不宜过长，必须设置，比如连接不够用时，时间过长将是灾难性的
//        clientHttpRequestFactory.setConnectionRequestTimeout(200);
//        // 缓冲请求数据，默认值是true。通过POST或者PUT大量发送数据时，建议将此属性更改为false，以免耗尽内存。
//        clientHttpRequestFactory.setBufferRequestBody(false);
//        return clientHttpRequestFactory;
//    }

    @Bean("https-request-factory")
    public HttpComponentsClientHttpRequestFactory generateHttpsRequestFactory() {
        try {
            // 使用 SSLContextBuilder 來建立 SSLContext
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true) // 信任所有證書
                    .build();

            // 建立 HttpClient
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE) // 不驗證主機名稱
                    .build();

            // 建立 HttpComponentsClientHttpRequestFactory
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(10 * 1000);
            factory.setReadTimeout(30 * 1000);
            return factory;
        } catch (Exception e) {
            log.error("generate HttpsRequestFactory failed! error msg:{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
