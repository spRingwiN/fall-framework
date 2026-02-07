package com.eric.fall.web;

import com.eric.fall.context.AnnotationConfigApplication;
import com.eric.fall.context.ApplicationContext;
import com.eric.fall.exception.NestedRuntimeException;
import com.eric.fall.io.PropertyResolver;
import com.eric.fall.web.utils.WebUtils;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextLoaderListener implements ServletContextListener {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("init {}.", getClass().getName());
        var servletContext = sce.getServletContext();
        WebMvcConfiguration.setServletContext(servletContext);

        var propertyResolver = WebUtils.createPropertyResolver();
        String encoding = propertyResolver.getProperty("${fall.web.character-encoding:UTF-8}");
        servletContext.setRequestCharacterEncoding(encoding);
        servletContext.setResponseCharacterEncoding(encoding);
        var applicationContext = createApplicationContext(servletContext.getInitParameter("configuration"), propertyResolver);
        // register Filter:
        WebUtils.registerFilter(servletContext);
        // register DispatcherServlet:
        WebUtils.registerDispatcherServlet(servletContext, propertyResolver);

        servletContext.setAttribute("applicationContext", applicationContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (sce.getServletContext().getAttribute("applicationContext") instanceof ApplicationContext applicationContext) {
            applicationContext.close();
        }
    }

    ApplicationContext createApplicationContext(String configClassName, PropertyResolver propertyResolver) {
        logger.info("init ApplicationContext by configuration: {}", configClassName);
        if (configClassName == null || configClassName.isEmpty()) {
            throw new NestedRuntimeException("Cannot init ApplicationContext for missing init param: configuration");
        }
        Class<?> configClass = null;
        try {
            configClass = Class.forName(configClassName);
        } catch (ClassNotFoundException e) {
            throw new NestedRuntimeException("Cannot load class from init param 'configuration': " + configClassName);
        }
        return new AnnotationConfigApplication(configClass, propertyResolver);
    }

}
