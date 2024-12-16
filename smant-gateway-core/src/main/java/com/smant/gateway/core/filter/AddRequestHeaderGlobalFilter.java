package com.smant.gateway.core.filter;

import com.google.common.collect.Maps;
import com.smant.common.core.constants.HttpHeaderKeys;
import com.smant.common.core.utils.StringExtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class AddRequestHeaderGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String systemCode = request.getHeaders().getFirst(HttpHeaderKeys.HTTP_HEADER_USYSTEM_CODE);
        String requestId = this.RequestId(systemCode);
        Map<String,String> headers = this.newHeaders(requestId);
        ServerHttpRequest newRequest = this.newRequest(request,headers);
        ServerWebExchange mutableExchange = exchange.mutate().request(newRequest).build();
        return chain.filter(mutableExchange);
    }

    private ServerHttpRequest newRequest(ServerHttpRequest request ,Map<String,String> headers){
        log.info("封装新的请求.....，添加新的请求头....");
        ServerHttpRequest.Builder builder = request.mutate();
        headers.forEach((key,value)->{
            builder.header(key,value);
        });
        return builder.build();
    }
    /**
     * 请求id
     * @param systemCode
     * @return
     */
    private  String RequestId(String systemCode){
        String requestId = UUID.randomUUID().toString().replaceAll("-","");
        return StringExtUtils.isEmpty(systemCode) ? requestId : systemCode+"-"+requestId;
    }
    private Map<String, String> newHeaders(String requestId) {
        Map<String, String> headers = Maps.newHashMap();
        log.info("添加用户请求id======"+requestId);
        headers.put(HttpHeaderKeys.HTTP_HEADER_REQUEST_ID, StringExtUtils.trimStr(requestId));
        return headers;
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
