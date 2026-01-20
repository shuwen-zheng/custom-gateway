package com.shuwen.gateway.vo;

import lombok.Data;
import reactor.netty.transport.ProxyProvider;

@Data
public class ProxyInfoVo {

    private ProxyProvider.Proxy type;

    private String host;

    private int port;

}
