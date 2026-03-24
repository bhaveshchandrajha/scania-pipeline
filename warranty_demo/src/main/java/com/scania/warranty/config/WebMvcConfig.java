package com.scania.warranty.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.util.Objects;

/**
 * Web MVC configuration for static resources and SPA support.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Angular SPA: serve /angular/** from static/angular/, fallback to index.html for client-side routing
        registry.addResourceHandler("/angular", "/angular/", "/angular/**")
                .addResourceLocations("classpath:/static/angular/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // Handle empty path or "." (from /angular/ or /angular)
                        if (resourcePath == null || resourcePath.isEmpty() || ".".equals(resourcePath)) {
                            return super.getResource("index.html", location);
                        }
                        Resource resource = super.getResource(resourcePath, location);
                        return Objects.isNull(resource) ? super.getResource("index.html", location) : resource;
                    }
                });
    }
}
