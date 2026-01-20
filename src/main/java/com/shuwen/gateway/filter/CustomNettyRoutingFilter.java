package com.shuwen.gateway.filter;

import com.shuwen.gateway.util.JsonUtils;
import com.shuwen.gateway.vo.ProxyInfoVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.config.HttpClientProperties;
import org.springframework.cloud.gateway.filter.NettyRoutingFilter;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.netty.http.client.HttpClient;

import java.util.List;
import java.util.Map;

import static com.shuwen.gateway.constant.Constant.KEY_PROXY;
import static com.shuwen.gateway.constant.Constant.KEY_SSL_SKIP;

/**
 * 自定义HttpClient
 * 1、设置请求是否跳过SSL证书验证
 * 2、设置请求是否需要web proxy
 * @author shuwen
 */
@Slf4j
public class CustomNettyRoutingFilter extends NettyRoutingFilter implements Ordered {

    public CustomNettyRoutingFilter(HttpClient httpClient, ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider, HttpClientProperties properties) {
        super(httpClient, headersFiltersProvider, properties);
    }

    @Override
    protected HttpClient getHttpClient(Route route, ServerWebExchange exchange) {
        HttpClient httpClient = super.getHttpClient(route, exchange);
        Boolean sslVerificationSkip = (Boolean) route.getMetadata().get(KEY_SSL_SKIP);
        if(sslVerificationSkip != null && sslVerificationSkip) {
            httpClient = createIgnoreSSLHttpClient(httpClient);
        }
        Map<String, Object> proxyInfo = (Map<String, Object>) route.getMetadata().get(KEY_PROXY);
        if(proxyInfo != null && !proxyInfo.isEmpty()) {
            try {
                ProxyInfoVo proxyInfoVo = JsonUtils.parseObject(JsonUtils.toJsonStr(proxyInfo), ProxyInfoVo.class);
                httpClient = httpClient.proxy(spec -> spec
                        .type(proxyInfoVo.getType())
                        .host(proxyInfoVo.getHost())
                        .port(proxyInfoVo.getPort()));
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException - ProxyInfoVo error!", e);
            }
        }

        return httpClient;
    }

    private static HttpClient createIgnoreSSLHttpClient(HttpClient httpClient) {
        try{
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            return httpClient.secure(spec -> spec.sslContext(sslContext));
        }catch (Exception e) {
            log.error("createIgnoreSSLHttpClient error!", e);
        }
        return httpClient;
    }

//    @Override
//    public int getOrder(){
//        return Ordered.HIGHEST_PRECEDENCE + 1;
//    }
}
