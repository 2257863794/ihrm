package com.ihrm.system;

import com.ihrm.common.util.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;

//1.配置springboot的包扫描
@SpringBootApplication(scanBasePackages = "com.ihrm")
//2.配置jpa注解的扫描,扫描实体类所在的包
@EntityScan(value="com.ihrm.domain.system")
//3.注册到eurake
@EnableEurekaClient
//4.找到其他的微服务【企业微服务】
@EnableDiscoveryClient
//5.开启调用
@EnableFeignClients

//配置了@EnableDiscoveryClient和@EnableFeignClients当前微服务就具有了调用其他微服务的能力

public class SystemApplication{

    /**
     * 启动方法
     */
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class,args);
    }

    @Bean
    public IdWorker idWorker(){
        return new IdWorker();
    }

//   @Bean
//    public JwtUtils jwtUtils(){
//         return new JwtUtils();
//    }
    //解决no session
    @Bean
    public OpenEntityManagerInViewFilter openEntityManagerInViewFilter() {
        return new OpenEntityManagerInViewFilter();
    }

}
