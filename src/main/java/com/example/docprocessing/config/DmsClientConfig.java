package com.example.docprocessing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;

import java.util.List;

@Configuration
public class DmsClientConfig {

    @Bean
    public RestClient dmsRestClient(DmsProperties dmsProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(dmsProperties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(dmsProperties.getReadTimeoutMs());

        ClientHttpRequestInterceptor apiKeyInterceptor = (request, body, execution) -> {
            request.getHeaders().add("X-API-KEY", dmsProperties.getApiKey());
            return execution.execute(request, body);
        };

        ClientHttpRequestFactory interceptingFactory =
                new InterceptingClientHttpRequestFactory(requestFactory, List.of(apiKeyInterceptor));

        return RestClient.builder()
                .baseUrl(dmsProperties.getBaseUrl())
                .requestFactory(interceptingFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }
}