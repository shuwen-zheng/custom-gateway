package com.shuwen.gateway.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

@Data
@Entity
@Table(name = "gateway_route")
@DynamicUpdate
@DynamicInsert
public class UadpGatewayRouteEntity {

    @Id
    private String id;

    @Column(name = "method")
    private String method;

    @Column(name = "inbound_path")
    private String inboundPath;

    @Column(name = "outbound_path")
    private String outboundPath;

    @Column(name = "order")
    private Integer orderNum;

    private String predicates;

    private Integer status;

    private String remark;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "create_by")
    private String createBy;

    @Column(name = "last_update_time")
    private Date lastUpdateTime;

    @Column(name = "last_update_by")
    private String lastUpdateBy;

    @Column(name = "is_logging")
    private Integer isLogging;

    private String metadata;

//    @Column(name = "server_name")
//    private String serverName;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "server_name", referencedColumnName = "server_name")
    private UadpGatewayServerEntity server;

}
