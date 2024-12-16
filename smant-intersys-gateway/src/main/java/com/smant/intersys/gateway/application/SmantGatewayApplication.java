package com.smant.intersys.gateway.application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RefreshScope
public class SmantGatewayApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmantGatewayApplication.class);
    public static void main(String[] args) {
        LOGGER.info("Smant  网关启动==========>");
        new SpringApplicationBuilder(SmantGatewayApplication.class)
                .main(SpringVersion.class)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}
