package com.phamthehuy.doan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${max.sec}")
    private long MAX_AGE_SECS;

    @Value("${client.url}")
    private String clientUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("HEAD", "OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE")
                .maxAge(MAX_AGE_SECS);
    }

//    @Override
//    public void addFormatters(FormatterRegistry registry) {
//        registry.addConverter(new StringToEnumConverter());
//    }
//
//    public static class StringToEnumConverter implements Converter<String, RoomType> {
//        @Override
//        public RoomType convert(String source) {
//            try {
//                return RoomType.valueOf(source.toUpperCase());
//            } catch (IllegalArgumentException e) {
//                throw new BadRequestException("Loại phòng không hợp lệ");
//            }
//        }
//    }
}
