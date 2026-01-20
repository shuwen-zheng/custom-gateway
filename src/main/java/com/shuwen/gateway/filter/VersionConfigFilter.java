package com.shuwen.gateway.filter;

import com.shuwen.gateway.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static com.shuwen.gateway.constant.Constant.KEY_VERSION;

/**
 * 设置load balancer version到request header，VersionBasedLoadBalancer需要
 */
@Component
@Slf4j
public class VersionConfigFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String version = (String) route.getMetadata().get(KEY_VERSION);
        ServerWebExchange mutatedExchange = exchange;
        if(StringUtils.hasText(version)) {
            mutatedExchange = exchange.mutate().request(request -> {
                HttpHeaders newHeaders = new HttpHeaders();
                HttpHeaders oldHttpHeaders = exchange.getRequest().getHeaders();
                oldHttpHeaders.forEach(newHeaders::put);
                if(StringUtils.hasText(version)) {
                    newHeaders.put(Constant.X_LOAD_BALANCER_VERSION, Collections.singletonList(version));
                }
                request.headers(headers -> headers.putAll(newHeaders));
            }).build();
        }
        log.info("VersionConfigFilter - version:" + version);
        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
