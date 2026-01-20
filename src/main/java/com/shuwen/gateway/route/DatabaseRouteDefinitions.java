package com.shuwen.gateway.route;

import com.shuwen.gateway.dao.GatewayRouteRepository;
import com.shuwen.gateway.dao.entity.UadpGatewayRouteEntity;
import com.shuwen.gateway.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static com.shuwen.gateway.constant.Constant.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseRouteDefinitions implements RouteDefinitionRepository {

    private final GatewayRouteRepository gatewayRouteRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 加载所有路由（Gateway启动时自动调用加载）
     * @return 路由定义的Flux版
     */
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        long loadStart = System.currentTimeMillis();
        //1、从DB读取所有启用的路由配置
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(Sort.Order.desc("inboundPath"));
        orders.add(Sort.Order.asc("orderNum"));
        List<UadpGatewayRouteEntity> dbRoutes = gatewayRouteRepository.findByStatus(1, Sort.by(orders));

        //2、DB配置转换为Gateway需要的RouteDefinition
        List<RouteDefinition> routeDefinitions = dbRoutes.stream()
                .map(this::convertToRouteDefinition)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("load gateway route time-consuming:" + (System.currentTimeMillis() - loadStart));
        //3、转换weiFlux

        return Flux.fromIterable(routeDefinitions);
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {

        return route.flatMap(routeDefinition -> {
            UadpGatewayRouteEntity uadpGatewayRouteEntity = convertToGatewayRouteEntity(routeDefinition);
            gatewayRouteRepository.save(uadpGatewayRouteEntity);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            Optional<UadpGatewayRouteEntity> routeEntity = gatewayRouteRepository.findById(id);
            if(routeEntity.isPresent()) {
                gatewayRouteRepository.deleteById(id);
            }else {
                return Mono.error(new NotFoundException("RouteDefinition not found:" + id));
            }
            return Mono.empty();
        });
    }

    private UadpGatewayRouteEntity convertToGatewayRouteEntity(RouteDefinition routeDefinition) {
        UadpGatewayRouteEntity uadpGatewayRouteEntity = new UadpGatewayRouteEntity();
//        routeDefinition.getId();
//        uadpGatewayRouteEntity.setId(routeDefinition.getId());
//        uadpGatewayRouteEntity.setInboundPath();
        return uadpGatewayRouteEntity;
    }
    private RouteDefinition convertToRouteDefinition(UadpGatewayRouteEntity dbRoute) {
        if(dbRoute == null || (dbRoute.getServer() != null && !StringUtils.hasText(dbRoute.getServer().getUri()))) {
            return null;
        }

        RouteDefinition routeDefinition = new RouteDefinition();
        //setting base information for route
        routeDefinition.setId(dbRoute.getId());

        routeDefinition.setUri(URI.create(dbRoute.getServer().getUri()));

        routeDefinition.setOrder(dbRoute.getOrderNum());

        //load predicates
        List<PredicateDefinition> predicates = loadBasePredicates(dbRoute);;
        if(StringUtils.hasText(dbRoute.getPredicates())) {
            try {
                predicates = objectMapper.readValue(dbRoute.getPredicates(), new TypeReference<List<PredicateDefinition>>() {});
            }catch (Exception e) {
                log.error("convertToRouteDefinition error, id:" + dbRoute.getId(), e);
            }
        }
        routeDefinition.setPredicates(predicates);

        //load base filter
        List<FilterDefinition> filters = loadBaseFilter(dbRoute);
        //add request body & response body print filters
        if(dbRoute.getIsLogging() != null && dbRoute.getIsLogging() == 1) {
            settingLoggingFilter(filters);
        }
        routeDefinition.setFilters(filters);

        //load metadata
        loadMetadata(routeDefinition, dbRoute);

        return routeDefinition;
    }

    private static void loadMetadata(RouteDefinition routeDefinition, UadpGatewayRouteEntity dbRoute) {
        Map<String, Object> metadata = new HashMap<>();
        if(StringUtils.hasText(dbRoute.getMetadata())) {
            try {
                metadata.putAll(JsonUtils.parseObject(dbRoute.getMetadata(), Map.class));
            }catch (Exception e) {
                log.error("loadMetadata - gateway metadata error!", e);
            }
        }

        if(dbRoute.getServer() != null && StringUtils.hasText(dbRoute.getServer().getMetadata())) {
            try {
                metadata.putAll(JsonUtils.parseObject(dbRoute.getServer().getMetadata(), Map.class));
            }catch (Exception e) {
                log.error("loadMetadata - gateway metadata error!", e);
            }
        }

        if(!metadata.isEmpty()) {
            routeDefinition.setMetadata(metadata);
        }
    }

    /**
     * 加载基础路由规则，RequestMethod & RequestPath is init Predicate
     * @param dbRoute
     */
    private static List<PredicateDefinition>  loadBasePredicates(UadpGatewayRouteEntity dbRoute) {
        List<PredicateDefinition> predicates = new ArrayList<>();
        PredicateDefinition methodPredicate = new PredicateDefinition();
        methodPredicate.setName(PREDICATE_METHOD);
        Map<String, String> methodPredicateArgs = new HashMap<>();
        methodPredicateArgs.put(ROUTE_ARG_0, dbRoute.getMethod());
        methodPredicate.setArgs(methodPredicateArgs);

        PredicateDefinition pathPredicate = new PredicateDefinition();
        pathPredicate.setName(PREDICATE_PATH);
        Map<String, String> pathPredicateArgs = new HashMap<>();
        pathPredicateArgs.put(ROUTE_ARG_0, dbRoute.getInboundPath());
        pathPredicate.setArgs(pathPredicateArgs);

        predicates.add(methodPredicate);
        predicates.add(pathPredicate);
        return predicates;
    }

    /**
     * base filter for route, if inbound path != outbound path , then set rewritePathFilter to setting outbound path
     * @param dbRoute
     */
    private static List<FilterDefinition> loadBaseFilter(UadpGatewayRouteEntity dbRoute) {
        List<FilterDefinition> filters = new ArrayList<>();
        if(StringUtils.hasText(dbRoute.getOutboundPath()) && !dbRoute.getInboundPath().equals(dbRoute.getOutboundPath())) {
            //如果inbound path和outbound path不一致，使用rewritePath filter实现
            FilterDefinition filterDefinition = new FilterDefinition();
            filterDefinition.setName(FILTER_REWRITE_PATH);
            Map<String, String> args = new HashMap<>();
            args.put(ROUTE_ARG_0, dbRoute.getInboundPath());
            args.put(ROUTE_ARG_1, dbRoute.getOutboundPath());
            filterDefinition.setArgs(args);
            filters.add(filterDefinition);
        }
        return filters;
    }

    private static void settingLoggingFilter(List<FilterDefinition> filters) {
        FilterDefinition requestLoggingFilter = loggingFilterDefinition(FILTER_MODIFY_REQUEST_BODY,"#{@requestLogger}");
        FilterDefinition responseLoggingFilter = loggingFilterDefinition(FILTER_MODIFY_RESPONSE_BODY, "#{@responseLogger}");
        filters.add(requestLoggingFilter);
        filters.add(responseLoggingFilter);
    }

    private static FilterDefinition loggingFilterDefinition(String filterName, String rewriteFunctionEl) {
        FilterDefinition responseLoggingFilter = new FilterDefinition();
        responseLoggingFilter.setName(filterName);
        Map<String, String> args = new HashMap<>();
        args.put("rewriteFunction", rewriteFunctionEl);
        responseLoggingFilter.setArgs(args);
        return responseLoggingFilter;
    }
}
