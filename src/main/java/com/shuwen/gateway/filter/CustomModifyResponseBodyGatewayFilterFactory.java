package com.shuwen.gateway.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 自定义ModifyResponseBodyFilter，解决ModifyResponseBodyFilter无法在FilterDefinition设置的问题
 * @author shuwen
 */
@Component
public class CustomModifyResponseBodyGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomModifyResponseBodyGatewayFilterFactory.Config> {

    private final ModifyResponseBodyGatewayFilterFactory delegate;

    public CustomModifyResponseBodyGatewayFilterFactory(ModifyResponseBodyGatewayFilterFactory delegate) {
        super(Config.class);
        this.delegate = delegate;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("rewriteFunction");
    }

    @Override
    public GatewayFilter apply(Config config) {
        ModifyResponseBodyGatewayFilterFactory.Config delegateConfig = new ModifyResponseBodyGatewayFilterFactory.Config();
        delegateConfig.setInClass(String.class);
        delegateConfig.setOutClass(String.class);
        delegateConfig.setRewriteFunction(String.class, String.class, config.rewriteFunction);
        return delegate.apply(delegateConfig);
    }

    @Data
    public static class Config {
        private RewriteFunction<String, String> rewriteFunction;
    }
}
