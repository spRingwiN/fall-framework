package com.eric.fall.web;

import com.eric.fall.annotation.Autowired;
import com.eric.fall.annotation.Bean;
import com.eric.fall.annotation.Configuration;
import com.eric.fall.annotation.Value;
import jakarta.servlet.ServletContext;

import java.util.Objects;

@Configuration
public class WebMvcConfiguration {

    private static ServletContext servletContext = null;

    /**
     * set by web listener
     * @param ctx
     */
    static void setServletContext(ServletContext ctx) {
        servletContext = ctx;
    }

    @Bean(initMethod = "init")
    ViewResolver viewResolver(
            @Autowired ServletContext servletContext,
            @Value("${fall.web.freemarker.template-path:/WEB-INF/templates}") String templatePath,
            @Value("${fall.web.freemarker.template-encoding:UTF-8}") String templateEncoding) {
        return new FreeMarkerViewResolver(templatePath, templateEncoding, servletContext);
    }

    @Bean
    ServletContext servletContext() {
        return Objects.requireNonNull(servletContext, "ServletContext is not set");
    }












}
