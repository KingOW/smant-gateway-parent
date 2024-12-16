package com.smant.gateway.core.filter;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Maps;
import com.smant.common.core.constants.CacheConstants;
import com.smant.common.core.constants.HttpHeaderKeys;
import com.smant.common.core.utils.IpUtils;
import com.smant.common.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import com.smant.common.core.utils.StringExtUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 操作日志 门户 过滤器
 */
@Component(value = "OperateLog")
@Slf4j
public class OperateLogGatewayFilterFactory extends AbstractGatewayFilterFactory<OperateLogGatewayFilterFactory.Config> {

    /**
     * 操作日志 gateway
     */
    public OperateLogGatewayFilterFactory() {
        super(OperateLogGatewayFilterFactory.Config.class);
    }

    @Autowired
    @Qualifier(value = "redisService")
    private RedisService redisService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public GatewayFilter apply(OperateLogGatewayFilterFactory.Config config) {

        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            long startTime = System.currentTimeMillis();
            this.CacheRequestInfo(request);
            log.info("用户操作================:校验令牌,请求路径=" + request.getURI());
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                long requetTime = System.currentTimeMillis() - startTime;
                this.HandlerRequestInfo(request, response, requetTime);
            }));
        };
    }

    //private static final String CACHE_REQUEST_INFO_KEY_SUFFIX = "Request-Info-";

    private Map<String, Object> CacheRequestInfo(ServerHttpRequest request) {
        String systemCode = request.getHeaders().getFirst(HttpHeaderKeys.HTTP_HEADER_USYSTEM_CODE);
        log.info("请求路径==" + request.getURI() + " 请求系统==" + systemCode);
        String requestId = request.getHeaders().getFirst(HttpHeaderKeys.HTTP_HEADER_REQUEST_ID);
        String loginToken = request.getHeaders().getFirst(HttpHeaderKeys.HTTP_HEADER_ULOGIN_TOKEN);
        String loginUserId = request.getHeaders().getFirst(HttpHeaderKeys.HTTP_HEADER_ULOGIN_USERID);
        String loginUserName = request.getHeaders().getFirst(HttpHeaderKeys.HTTP_HEADER_ULOGIN_USERNAME);
        Map<String, Object> requestInfo = Maps.newHashMap();

        requestInfo.put(CacheConstants.CACHE_KEY_SYSTEM_CODE, StringExtUtils.trimStr(systemCode));
        requestInfo.put(CacheConstants.CACHE_KEY_LOGIN_TOKEN, StringExtUtils.trimStr(loginToken));
        requestInfo.put(CacheConstants.CACHE_KEY_REQUEST_USER_ID, StringExtUtils.trimStr(loginUserId));
        requestInfo.put(CacheConstants.CACHE_KEY_REQUEST_USER_NAME, StringExtUtils.trimStr(loginUserName));
//        requestInfo.put("", StringExtUtils.trimStr(systemCode));
        requestInfo.put(CacheConstants.CACHE_KEY_REQUEST_ID, StringExtUtils.trimStr(requestId));
        requestInfo.put(CacheConstants.CACHE_KEY_REQUEST_HOST, request.getHeaders().getFirst("Host"));
//        String requestIp = IpUtils.getIpAddr(request);
        requestInfo.put(CacheConstants.CACHE_KEY_REQUEST_IP, IpUtils.getIpAddr(request));
        requestInfo.put(CacheConstants.CACHE_KEY_REQUEST_URL, StringExtUtils.trimStr(request.getURI().toString()));
        requestInfo.put(CacheConstants.CACHE_KEY_REQUEST_PATH, StringExtUtils.trimStr(request.getPath().value()));
        requestInfo.put(CacheConstants.CACHE_KEY_REQUEST_START_TIME, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        requestInfo.put(CacheConstants.CACHE_KEY_REQUEST_QUERY_PARAMS, StringExtUtils.trimStr(request.getQueryParams().toString()));
        try {
            log.info("请求数据：" + JSONObject.toJSONString(requestInfo));
            this.redisService.setCacheMap(CacheConstants.CACHE_KEY_REQUEST_INFO_PREFIX + requestId, requestInfo, 600L);
            this.redisService.expire(CacheConstants.CACHE_KEY_REQUEST_INFO_PREFIX + requestId, 1, TimeUnit.HOURS);//1个小时有效期
        } catch (Exception ex) {
            log.error("保存请求数据到缓存中失败：服务器出现异常.", ex);
        }
        return requestInfo;
    }

    private void HandlerRequestInfo(ServerHttpRequest request, ServerHttpResponse origResponse, long requestTime) {
        String requestId = request.getHeaders().getFirst(HttpHeaderKeys.HTTP_HEADER_REQUEST_ID);
//        String loginToken = request.getHeaders().getFirst(HttpHeaderKeys.HTTP_HEADER_ULOGIN_TOKEN);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try {
            Map<String, Object> infoMap = Maps.newHashMap();
            infoMap.put(CacheConstants.CACHE_KEY_REQUEST_FINISH_TIME, now);
            infoMap.put(CacheConstants.CACHE_KEY_REQUEST_TIME, requestTime + "");
            infoMap.put(CacheConstants.CACHE_KEY_REQUEST_STATUS, origResponse.getStatusCode().value() + "");
            DataBufferFactory bufferFactory = origResponse.bufferFactory();
            ServerHttpResponseDecorator response = new ServerHttpResponseDecorator(origResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (getStatusCode().equals(HttpStatus.OK) && body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        return super.writeWith(fluxBody.map(dataBuffer -> {
                            // probably should reuse buffers
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            //释放掉内存
                            DataBufferUtils.release(dataBuffer);
                            String resResponse = new String(content, Charset.forName("UTF-8"));
                            log.info("响应结果=====>"+resResponse);
                            byte[] uppedContent = resResponse.getBytes();
                            return bufferFactory.wrap(uppedContent);
                        }));
                    }
                    return super.writeWith(body);
                }

                @Override
                public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                    return writeWith(Flux.from(body).flatMapSequential(p -> p));
                }
            };
            this.redisService.setCacheMap(CacheConstants.CACHE_KEY_REQUEST_INFO_PREFIX + requestId, infoMap, 600);
            log.info("缓存请求数据" + requestTime);
//            Map<String, String> msgMap = Maps.newHashMap();
//            msgMap.put(KafkaConstants.KAFKA_MESSAGE_KEY_REQUEST_INFO_KEY, CacheConstants.CACHE_KEY_REQUEST_INFO_SUFFIX + requestId);
//            kafkaTemplate.send(KafkaConstants.KAFKA_TOPIC_SAVE_OPERLOG, JSONObject.toJSONString(msgMap));
        } catch (Exception ex) {
            log.error("保存请求数据到缓存中失败：服务器出现异常.", ex);
        }

    }

    static class Config {
    }
}
