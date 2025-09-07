/*
package com.jiawa.train.common.config;// package com.jiawa.train.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

*/
/*同一注解，解决前后端long类型精度问题*//*

 @Configuration
 public class JacksonConfig {
     @Bean
     public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
         ObjectMapper objectMapper = builder.createXmlMapper(false).build();
         SimpleModule simpleModule = new SimpleModule();
         simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
         objectMapper.registerModule(simpleModule);
         return objectMapper;
     }
 }
*/
