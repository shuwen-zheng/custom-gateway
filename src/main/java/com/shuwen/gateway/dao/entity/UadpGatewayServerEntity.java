package com.shuwen.gateway.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Data
@Entity
@Table(name = "gateway_server")
@DynamicUpdate
@DynamicInsert
public class UadpGatewayServerEntity {

    @Id()
    @Column(name = "server_name")
    private String serverName;

    @Column(name = "uri")
    private String uri;

    private String desc;

    private String metadata;


}
