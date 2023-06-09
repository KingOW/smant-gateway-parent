package com.smant.gateway.admin.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.SpringVersion;

@SpringBootApplication(
        scanBasePackages = {"com.smant.gateway.admin"},exclude = {DataSourceAutoConfiguration.class })
@EnableDiscoveryClient
@Slf4j
public class AdminGatewayApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(AdminGatewayApplication.class)
                .main(SpringVersion.class)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}
