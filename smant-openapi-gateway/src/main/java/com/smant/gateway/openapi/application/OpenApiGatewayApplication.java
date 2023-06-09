package com.smant.gateway.openapi.application;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.SpringVersion;

@SpringBootApplication(
        scanBasePackages = {"com.smant.gateway.openapi"},exclude = {DataSourceAutoConfiguration.class })
@Slf4j
public class OpenApiGatewayApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(OpenApiGatewayApplication.class)
                .main(SpringVersion.class)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}
