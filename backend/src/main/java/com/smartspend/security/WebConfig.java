package com.smartspend.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Mapea la ruta raíz y cualquier otra ruta no resuelta a index.html
        // Esto es crucial para el enrutamiento de Angular (SPA)
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/{path:[^\\.]*}").setViewName("forward:/index.html");
        // Corregido: Usa nombres de variables diferentes para los segmentos de la ruta
        registry.addViewController("/{path1:.*}/{path2:[^\\.]*}").setViewName("forward:/index.html");
    }
}