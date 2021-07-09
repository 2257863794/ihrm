package com.ihrm.employee;

import com.ihrm.common.util.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

//1.配置springboot的包扫描
@SpringBootApplication(scanBasePackages = "com.ihrm")
//2.配置jpa注解的扫描,扫描实体类所在的包
@EntityScan(value="com.ihrm.domain")
//注册到eurake
@EnableEurekaClient
public class EmployeeApplication {

    /**
     * 启动方法
     */
    public static void main(String[] args) {
        SpringApplication.run(EmployeeApplication.class,args);
    }

    @Bean
    public IdWorker idWorker(){
        return new IdWorker();
    }

}
