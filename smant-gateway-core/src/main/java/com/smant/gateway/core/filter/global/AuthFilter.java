package com.smant.gateway.core.filter.global;

import com.alibaba.fastjson2.JSONObject;
import com.smant.auth.client.TokenAuthClient;
import com.smant.auth.core.dto.UserTokenDto;
import com.smant.auth.core.model.LoginUser;
import com.smant.common.beans.ResultBean;
import com.smant.common.enums.CommKeys;
import com.smant.common.enums.CommResultCode;
import com.smant.common.utils.FastJsonUtils;
import com.smant.common.utils.StringExtUtils;
import com.smant.gateway.core.config.properties.IgnoreWhiteProperties;
import com.smant.sdk.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * 安全 filter
 */
@Component
@Slf4j
public class AuthFilter implements Ordered, GlobalFilter {


    /**
     * 白名单
     */
    @Autowired
    private IgnoreWhiteProperties whiteProperties;

    /**
     * auth 客户端
     */
    @Autowired
    private TokenAuthClient tokenAuthClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String traceId = headers.getFirst(CommKeys.HttpHeaderKey.HTTP_HEADER_TRACE_ID.getKeyCode());
        log.info("traceId={},用户校验:检查请求地址是否在白名单中",traceId);
        if (this.checkURL(exchange)) {
            log.info("traceId={},用户校验:请求地址:{},在白名单中,不进行校验",traceId,exchange.getRequest().getURI());
            return chain.filter(exchange);
        }
        log.info("traceId={},用户校验:校验token是否符合合法",traceId);
        String token = headers.getFirst("Authorization");
        String sysCode = headers.getFirst(CommKeys.HttpHeaderKey.HTTP_HEADER_SYSTEM_CODE.getKeyCode());
        ResultBean<LoginUser> authResult = tokenAuthClient.authToken(new UserTokenDto(sysCode, token));
        if (authResult == null || authResult.isNotSuccess()) {
            log.warn("traceId={},用户校验失败",traceId);
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.OK);
            DataBuffer bodyDataBuffer = response.bufferFactory().wrap(FastJsonUtils.toJsonString(authResult).getBytes());
            return response.writeWith(Mono.just(bodyDataBuffer));
        }
        log.info("traceId={},用户校验成功,封装响应信息.",traceId);
        LoginUser loginUser = authResult.getData();
        exchange.getRequest().mutate().header(CommKeys.HttpHeaderKey.HTTP_HEADER_USER_INFO.getKeyCode(), FastJsonUtils.toJsonString(loginUser));
        exchange.getRequest().mutate().header(CommKeys.HttpHeaderKey.HTTP_HEADER_USERID.getKeyCode(), loginUser.getUserId());
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 10;
    }

    private boolean checkURL(ServerWebExchange exchange) {
        return StringExtUtils.matchStr(exchange.getRequest().getURI().toString(),this.whiteProperties.getWhites());
    }
}
