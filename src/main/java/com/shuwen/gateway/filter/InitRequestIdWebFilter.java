package com.shuwen.gateway.filter;

import com.shuwen.gateway.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

/**
 * 生成Request ID(如果请求不存在X-Request-ID), 并设置到request header
 *
 * @author shuwen
 */
@Component
@Slf4j
public class InitRequestIdWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long currentTime = System.currentTimeMillis();
        String requestId = exchange.getRequest().getHeaders().getFirst(Constant.X_REQUEST_ID);

        ServerWebExchange mutatedExchange = exchange;
        if(!StringUtils.hasText(requestId)) {
            requestId = String.valueOf(currentTime) + UUID.randomUUID().toString().hashCode();
            String finalRequestId = requestId;
            mutatedExchange = exchange.mutate().request(request -> {
                HttpHeaders newHeaders = new HttpHeaders();
                HttpHeaders oldHttpHeaders = exchange.getRequest().getHeaders();
                oldHttpHeaders.forEach(newHeaders::put);
                newHeaders.put(Constant.X_REQUEST_ID, Collections.singletonList(finalRequestId));
                MDC.put("requestId", finalRequestId);
                request.headers(headers -> headers.putAll(newHeaders));
            }).build();
        }
        log.info("RequestIdGenerateFilter - request id: {}", requestId);
        return chain.filter(mutatedExchange);
    }

}
