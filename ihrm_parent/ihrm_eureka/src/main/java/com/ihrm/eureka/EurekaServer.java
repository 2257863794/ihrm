package com.ihrm.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * eureka服务的的启动类
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableEurekaServer //开启eureka服务端配置
public class EurekaServer {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServer.class,args);
    }
}
