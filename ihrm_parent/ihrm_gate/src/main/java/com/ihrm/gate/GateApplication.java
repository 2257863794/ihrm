package com.ihrm.gate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

//1.配置springboot的包扫描
@SpringBootApplication(scanBasePackages = "com.ihrm")
//2.开启zuul网关功能
@EnableZuulProxy
//3.开启服务发现功能
@EnableDiscoveryClient
public class GateApplication {

    /**
     * 启动方法
     */
    public static void main(String[] args) {
        SpringApplication.run(GateApplication.class,args);
    }

}
