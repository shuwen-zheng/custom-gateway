package com.shuwen.gateway.config;

import com.shuwen.gateway.constant.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

import static com.shuwen.gateway.constant.Constant.X_LOAD_BALANCER_VERSION;

@RequiredArgsConstructor
@Slf4j
public class VersionBasedLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final String serviceId;

    private final ObjectProvider<ServiceInstanceListSupplier> objectProvider;

    private final SecureRandom random = new SecureRandom();

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {

        RequestDataContext context = (RequestDataContext) request.getContext();

        return objectProvider.getIfAvailable().get()
                .next()
                .map(serviceInstances -> {
                    String lbVersion = context.getClientRequest().getHeaders().getFirst(X_LOAD_BALANCER_VERSION);

                    List<ServiceInstance> filteredInstances = filterByVersion(serviceInstances, lbVersion);

                    if(filteredInstances.isEmpty()){
                        log.info("VersionBasedLoadBalancer - service:{}, not found matched version instance, version: {}, all instances: {}", serviceId, lbVersion, serviceInstances);
                        return new EmptyResponse();
                    }

                    ServiceInstance selected = selectRandomInstance(filteredInstances);
                    log.info("VersionBasedLoadBalancer - service:{}, selected version:{}, instance:{}", selected, lbVersion, selected);
                    return new DefaultResponse(selected);
                });
    }


    private List<ServiceInstance> filterByVersion(List<ServiceInstance> instances, String targetVersion) {
        //如果沒有设置目标版本/ALL， 返回所有实例
        if(targetVersion == null || "all".equalsIgnoreCase(targetVersion)) {
            return instances;
        }
        //过滤符合规范的版本
        return instances.stream()
                .filter(instance -> {
                    String instanceVersion = instance.getMetadata().get(Constant.KEY_VERSION);
                    return targetVersion.equals(instanceVersion);
                })
                .collect(Collectors.toList());
    }

    private ServiceInstance selectRandomInstance(List<ServiceInstance> instances) {
        if(instances.size() == 1) {
            return instances.get(0);
        }
        int index = random.nextInt(instances.size());
        return instances.get(index);
    }
}
