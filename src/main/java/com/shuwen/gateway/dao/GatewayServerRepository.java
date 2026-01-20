package com.shuwen.gateway.dao;

import com.shuwen.gateway.dao.entity.UadpGatewayServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayServerRepository extends JpaRepository<UadpGatewayServerEntity, String> {



}
