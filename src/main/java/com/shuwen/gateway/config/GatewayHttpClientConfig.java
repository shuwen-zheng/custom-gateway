package com.shuwen.gateway.config;

import com.shuwen.gateway.filter.CustomNettyRoutingFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.config.HttpClientProperties;
import org.springframework.cloud.gateway.filter.NettyRoutingFilter;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.netty.http.client.HttpClient;

import java.util.List;


@Configuration
public class GatewayHttpClientConfig {

    @Bean
    @Primary
    public NettyRoutingFilter customNettyRoutingFilter(HttpClient httpClient, ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider, HttpClientProperties properties) {
        return new CustomNettyRoutingFilter(httpClient, headersFiltersProvider, properties);
    }
}
