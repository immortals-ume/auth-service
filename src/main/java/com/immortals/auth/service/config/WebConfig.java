package com.immortals.auth.service.config;

import com.immortals.auth.service.security.interceptor.ClientIpInterceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final ClientIpInterceptor ipAddressInterceptor;

    @Value("${auth.cache.static-resources.max-age-seconds}")
    private Long maxAgeInSeconds;

    public WebConfig(ClientIpInterceptor ipAddressInterceptor) {
        this.ipAddressInterceptor = ipAddressInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(maxAgeInSeconds, TimeUnit.SECONDS)
                        .cachePublic()
                        .immutable())
                .resourceChain(Boolean.TRUE)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
    }

    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ipAddressInterceptor);
    }
}

