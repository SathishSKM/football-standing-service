package com.ps.footballstanding.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Value("${client.api.football.connect-timeout-ms:5000}")
    private int apiFootballConnectTimeout;

    @Value("${client.api.football.read-timeout-ms:10000}")
    private int apiFootballReadTimeout;


    @Bean("apiFootballRestTemplate")
    public RestTemplate apiFootballRestTemplate() {
        return buildRestTemplate(
                apiFootballConnectTimeout,
                apiFootballReadTimeout
        );
    }

    private RestTemplate buildRestTemplate(int connectTimeout, int readTimeout) {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeout))
                .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }

}
