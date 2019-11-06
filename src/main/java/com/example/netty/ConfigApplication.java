package com.example.netty;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfigApplication implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/data/glory/**").addResourceLocations("file:E:/data/glory/");
        registry.addResourceHandler("/data/**").addResourceLocations("file:/data/");
    }
}
