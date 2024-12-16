package com.smant.intersys.gateway.application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.SpringVersion;
@SpringBootApplication(scanBasePackages = {
        "com.smant.intersys.gateway",
        "com.smant.gateway.core",
        "com.smant.common.redis",
        "com.smant.common.core"},exclude = { DataSourceAutoConfiguration.class})
@Slf4j
@RefreshScope
public class SmantGatewayApplication {
    public static void main(String[] args) {
        log.info("Smant  网关启动==========>");
        new SpringApplicationBuilder(SmantGatewayApplication.class)
                .main(SpringVersion.class)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}
