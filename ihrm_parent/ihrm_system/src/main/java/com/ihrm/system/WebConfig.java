package com.ihrm.system;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihrm.common.converter.GlobalFormDateConvert;
import com.ihrm.common.converter.GlobalJsonDateConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Configuration
public class WebConfig {

 //JSON格式 全局日期转换器配置
 @Bean
 public MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
  MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
  //设置日期格式
  ObjectMapper objectMapper = new ObjectMapper();
  objectMapper.setDateFormat(GlobalJsonDateConvert.instance);
  objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
  //设置中文编码格式
  List<MediaType> list = new ArrayList<MediaType>();
  list.add(MediaType.APPLICATION_JSON_UTF8);
  mappingJackson2HttpMessageConverter.setSupportedMediaTypes(list);
  return mappingJackson2HttpMessageConverter;
 }

 //表单格式 全局日期转换器

 @Bean
 @Autowired
 public ConversionService getConversionService(GlobalFormDateConvert globalDateConvert){
  ConversionServiceFactoryBean factoryBean = new ConversionServiceFactoryBean();
  Set<Converter> converters = new HashSet<>();
  converters.add(globalDateConvert);
  factoryBean.setConverters(converters);
  return factoryBean.getObject();
 }
}