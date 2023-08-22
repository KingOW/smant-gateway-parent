package com.smant.gateway.core.filter.global;

import com.smant.common.constants.CommConstants;
import com.smant.common.enums.CommKeys;
import com.smant.common.utils.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RequestLogFilter implements GlobalFilter, Ordered {

    //private static final String REQUEST_TIME_START = "RequestTimeStart";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        String traceId = UUIDUtils.getUUID();//请求id
        request.getHeaders().add(CommKeys.HttpHeaderKey.HTTP_HEADER_TRACE_ID.getKeyCode(),traceId);
        log.info("url={},requestId={},startTime={}",exchange.getRequest().getURI(),exchange.getRequest().getId(),startTime);
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
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
