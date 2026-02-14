package com.eric.fall.web;

import com.eric.fall.context.AnnotationConfigApplication;
import com.eric.fall.context.ApplicationContext;
import com.eric.fall.io.PropertyResolver;
import com.eric.fall.web.utils.WebUtils;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ContextLoaderInitializer implements ServletContainerInitializer {

    final Logger logger = LoggerFactory.getLogger(getClass());
    final Class<?> configClass;
    final PropertyResolver propertyResolver;

    public ContextLoaderInitializer(Class<?> configClass, PropertyResolver propertyResolver) {
        this.configClass = configClass;
        this.propertyResolver = propertyResolver;
    }



    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        logger.info("Servlet container start. ServletContext = {}", servletContext);

        String encoding = propertyResolver.getProperty("${fall.web.character-encoding:UTF-8}", String.class);
        servletContext.setRequestCharacterEncoding(encoding);
        servletContext.setResponseCharacterEncoding(encoding);

        WebMvcConfiguration.setServletContext(servletContext);
        ApplicationContext applicationContext = new AnnotationConfigApplication(this.configClass, this.propertyResolver);
        logger.info("ApplicationContext created: {}", applicationContext);

        // register filters:
        WebUtils.registerFilter(servletContext);
        // register DispatcherServlet:
        WebUtils.registerDispatcherServlet(servletContext, this.propertyResolver);
    }
}
