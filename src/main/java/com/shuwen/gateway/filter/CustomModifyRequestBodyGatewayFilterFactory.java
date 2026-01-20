package com.shuwen.gateway.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 自定义ModifyRequestBodyFilter，解决ModifyRequestBodyFilter无法在FilterDefinition设置的问题
 * @author shuwen
 */
@Component
public class CustomModifyRequestBodyGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomModifyRequestBodyGatewayFilterFactory.Config> {

    private final ModifyRequestBodyGatewayFilterFactory delegate;

    public CustomModifyRequestBodyGatewayFilterFactory(ModifyRequestBodyGatewayFilterFactory delegate) {
        super(Config.class);
        this.delegate = delegate;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("rewriteFunction");
    }

    @Override
    public GatewayFilter apply(Config config) {
        ModifyRequestBodyGatewayFilterFactory.Config delegateConfig = new ModifyRequestBodyGatewayFilterFactory.Config();
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
