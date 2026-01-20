package com.shuwen.gateway.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * 1、处理不符合URI规范的serviceName
 *   不符合URI规范的serviceName需要先转换为合法URI， LoadBalancer时再转换为真实的serviceName，即录入非法serviceName也需要先转换为合法URI存入数据库中
 * 2、启用自定义的VersionBasedLoadBalancer
 * @author shuwen
 */
@Configuration
@LoadBalancerClients(defaultConfiguration = CustomLoadBalancerConfig.class)
public class CustomLoadBalancerConfig {

    private static final Map<String, String> SERVICE_NAME_MAPPING = new HashMap<>();

    static {
        SERVICE_NAME_MAPPING.put("native-dbank-service", "NATIVE_DBANK-SERVICE");
        SERVICE_NAME_MAPPING.put("native-dbank-customer", "NATIVE_DBANK-CUSTOMER");
        SERVICE_NAME_MAPPING.put("native-dbank-investment", "NATIVE_DBANK-INVESTMENT");
        SERVICE_NAME_MAPPING.put("native-dbank-operation", "NATIVE_DBANK-OPERATION");
        SERVICE_NAME_MAPPING.put("native-dbank-payment", "NATIVE_DBANK-PAYMENT");
        SERVICE_NAME_MAPPING.put("native-dbank-account-opening", "NATIVE_DBANK-ACCOUNT-OPENING");
    }

    /**
     * 创建自定义的ReactorLoadBalancer
     * @param environment current serviceName env
     * @param loadBalancerClientFactory
     * @return
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> reactorServiceInstanceLoadBalancer(Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {

        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);

        String actualServiceName = SERVICE_NAME_MAPPING.getOrDefault(name, name);

        ObjectProvider<ServiceInstanceListSupplier> objectProvider = loadBalancerClientFactory.getLazyProvider(actualServiceName, ServiceInstanceListSupplier.class);

        return new VersionBasedLoadBalancer(actualServiceName, objectProvider);
    }

}
