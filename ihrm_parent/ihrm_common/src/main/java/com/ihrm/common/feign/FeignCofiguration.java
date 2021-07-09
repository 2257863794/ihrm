package com.ihrm.common.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.rmi.server.ServerCloneException;
import java.util.Enumeration;

@Configuration
public class FeignCofiguration {
    //配置feign拦截器，解决请求头问题
    @Bean
   public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            //获取所有浏览器发送的请求属性，请求头赋值到feign调用的过程中
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //得到所有的请求属性
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                //请求属性不为null
                if(attributes !=null){
                    //重请求属性中，得到请求对象
                    HttpServletRequest request= attributes.getRequest();
                    //获取浏览器发起的请求头,得到所有的请求头的名称
                    Enumeration<String> headerNames = request.getHeaderNames();
                    if(headerNames !=null){
                        while (headerNames.hasMoreElements()){
                            //获取请求头的名称 Authorization
                            String name = headerNames.nextElement();
                            //根据请求头的名称，获取请求的数据, "Bearer bldbb4cf-7de6-41e5-99e2-0e8b7e8fe6ee"
                            String value = request.getHeader(name);
                            requestTemplate.header(name,value);
                        }
                    }
                }
            }
        };
    }
}
