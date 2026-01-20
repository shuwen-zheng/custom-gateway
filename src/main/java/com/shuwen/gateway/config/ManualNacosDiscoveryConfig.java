package com.shuwen.gateway.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 手动注册Nacos的Discovery能力
 * 关闭服务自动注册功能之后，Nacos的服务发现能力也会一起关闭，需要手动拉起，
 * service-registry.auto-registration.enabled: false
 * @author shuwen 
 */
@Configuration
public class ManualNacosDiscoveryConfig {


    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Bean
    public NamingService namingService() throws NacosException {
        return new NacosNamingService(nacosDiscoveryProperties.getNacosProperties());
    }

    @Bean
    public NacosServiceManager nacosServiceManager() {
        NacosServiceManager nacosServiceManager = new NacosServiceManager();
        nacosServiceManager.setNacosDiscoveryProperties(nacosDiscoveryProperties);
        return nacosServiceManager;
    }

    @Bean
    public NacosDiscoveryClient nacosDiscoveryClient(NacosServiceManager nacosServiceManager) throws NacosException {
        nacosDiscoveryProperties.setInstanceEnabled(true);
        return new NacosDiscoveryClient(new NacosServiceDiscovery(nacosDiscoveryProperties, nacosServiceManager));
    }

}
