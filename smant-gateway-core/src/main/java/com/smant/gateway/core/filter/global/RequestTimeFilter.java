package com.smant.gateway.core.filter.global;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RequestTimeFilter implements Ordered, GlobalFilter {

    private static final String REQUEST_TIME_START = "RequestTimeStart";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        //exchange.getAttributes().put(REQUEST_TIME_START, System.currentTimeMillis());
        log.info("url={},requestId={},startTime={}",exchange.getRequest().getURI(),exchange.getRequest().getId(),startTime);
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    //long startTime_ = exchange.getAttribute(REQUEST_TIME_START);
                    long endTime = System.currentTimeMillis();
                    log.info("url={},requestId={},requestTime={}",exchange.getRequest().getURI() ,exchange.getRequest().getId(), (endTime - startTime) + "ms");
                })
        );
    }
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }


}