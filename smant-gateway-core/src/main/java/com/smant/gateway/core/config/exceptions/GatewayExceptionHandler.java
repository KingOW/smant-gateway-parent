package com.smant.gateway.core.config.exceptions;

import com.google.common.collect.Maps;
import com.smant.common.enums.CommResultCode;
import com.smant.common.exceptions.SmantBaseException;
import com.smant.common.utils.DateUtils;
import com.smant.common.utils.FastJsonUtils;
import com.smant.common.utils.StringExtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.net.UnknownHostException;
import java.util.Map;


@Slf4j
public class GatewayExceptionHandler extends DefaultErrorWebExceptionHandler {
    public GatewayExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties,
                                       ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, webProperties.getResources(), errorProperties, applicationContext);
    }


    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String,Object> errorAttributes = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        return ServerResponse
                // http返回码
                .status(200)
                // 类型和以前一样
                .contentType(MediaType.APPLICATION_JSON)
                // 响应body的内容
                .body(BodyInserters.fromValue(errorAttributes));
    }

    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        String errorMessage = CommResultCode.SERVER_ERROR.getMsg();
        // 返回码
        int code = CommResultCode.SERVER_ERROR.getCode();
        Map<String, Object> errorAttributes = Maps.newHashMap();
        Throwable error = super.getError(request);
        if (error instanceof UnknownHostException) {
            errorMessage =  StringExtUtils.containStr(error.getMessage(), "未知的名称或服务")
                    ? error.getMessage() : error.getMessage() + " 未知的名称或服务";
        } else if(error instanceof SmantBaseException){
            SmantBaseException ex = (SmantBaseException) error;
            code = ex.getErrCode();
            errorMessage = ex.getErrMessage();
        }
        errorAttributes.put("timestamp", DateUtils.getCurrentDateStr());
        errorAttributes.put("method", request.method().toString());
        errorAttributes.put("path", request.path());
        errorAttributes.put("requestId", request.exchange().getRequest().getId());
        errorAttributes.put("status", code);
        errorAttributes.put("code", code);
        errorAttributes.put("message", errorMessage);
        errorAttributes.put("success", false);
        errorAttributes.put("notSuccess", true);
        errorAttributes.put("data", null);
        log.error("全局异常处理：" + FastJsonUtils.toJson(errorAttributes),error);
        return errorAttributes;
    }





    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }


    //    @Override
//    protected RouterFunction<ServerResponse> getRoutingFunction(
//            ErrorAttributes errorAttributes) {
//        return RouterFunctions.route(acceptsTextHtml(), this::renderErrorView)
//                .andRoute(RequestPredicates.all(), this::renderErrorResponse);
//    }
}
