package com.smant.gateway.admin.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.SpringVersion;

@SpringBootApplication(
        scanBasePackages = {
                "com.smant.gateway.admin",
                "com.smant.gateway.core.config",
                "com.smant.gateway.core.filter.global"},exclude = {DataSourceAutoConfiguration.class })
@Slf4j
@EnableFeignClients(basePackages = {"com.smant.auth.client"})
public class AdminGatewayApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(AdminGatewayApplication.class)
                .main(SpringVersion.class)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}
