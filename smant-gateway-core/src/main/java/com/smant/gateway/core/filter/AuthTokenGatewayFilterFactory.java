package com.smant.gateway.core.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component(value = "AuthToken")
@Slf4j
public class AuthTokenGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthTokenGatewayFilterFactory.Config> {
    @Override
    public GatewayFilter apply(Config config) {
        return null;
    }

    static class Config {

    }
}
