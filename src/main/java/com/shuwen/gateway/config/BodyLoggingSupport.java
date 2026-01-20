package com.shuwen.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shuwen.gateway.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

/**
 * 请求 & 响应 报文打印
 * @author shuwen
 */
@Configuration
@Slf4j
public class BodyLoggingSupport {


    @Bean("requestLogger")
    public RewriteFunction<String, String> requestLogger() {
        return (exchange, body) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            try {
                log.info("request headers: [{}],\n request body: [{}]", JsonUtils.toJsonStr(headers), body);
            } catch (JsonProcessingException e) {
                log.error("BodyLoggingSupport - requestLogger error!", e);
            }

            return body == null ? Mono.empty() : Mono.just(body);
        };
    }

    @Bean("responseLogger")
    public RewriteFunction<String, String> responseLogger() {
        return (exchange, body) -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            try {
                log.info("response headers: [{}],\n response body: [{}]", JsonUtils.toJsonStr(headers), body);
            } catch (JsonProcessingException e) {
                log.error("BodyLoggingSupport - responseLogger error!", e);
            }
            return body == null ? Mono.empty() : Mono.just(body);
        };
    }

}
