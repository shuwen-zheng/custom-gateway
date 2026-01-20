package com.shuwen.gateway.dao;

import com.shuwen.gateway.dao.entity.UadpGatewayRouteEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GatewayRouteRepository extends JpaRepository<UadpGatewayRouteEntity, String> {

    List<UadpGatewayRouteEntity> findByStatus(Integer status, Sort sort);

}
