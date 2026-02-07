package com.eric.fall.web;

import com.eric.fall.annotation.*;
import com.eric.fall.context.ApplicationContext;
import com.eric.fall.context.ConfigurableApplicationContext;
import com.eric.fall.exception.ErrorResponseException;
import com.eric.fall.exception.NestedRuntimeException;
import com.eric.fall.exception.ServerErrorException;
import com.eric.fall.exception.ServerWebInputException;
import com.eric.fall.io.PropertyResolver;
import com.eric.fall.utils.ClassUtils;
import com.eric.fall.web.utils.JsonUtils;
import com.eric.fall.web.utils.PathUtils;
import com.eric.fall.web.utils.WebUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatcherServlet extends HttpServlet {

    final Logger logger = LoggerFactory.getLogger(getClass());

    ApplicationContext applicationContext;
    ViewResolver viewResolver;

    String resourcePath;
    String faviconPath;

    List<Dispatcher> getDispatchers = new ArrayList<>();
    List<Dispatcher> postDispatchers = new ArrayList<>();



    public DispatcherServlet(ApplicationContext applicationContext, PropertyResolver propertyResolver) {
        this.applicationContext = applicationContext;
        this.viewResolver = applicationContext.getBean(ViewResolver.class);
        this.resourcePath = propertyResolver.getProperty("${fall.web.static-path:/static/}");
        this.faviconPath = propertyResolver.getProperty("${fall.web.favicon-path:/favicon.ico}");
        if (!this.resourcePath.endsWith("/")) {
            this.resourcePath += "/";
        }
    }

    @Override
    public void init() throws ServletException {
        logger.info("init {}", getClass().getName());
        // scan @Controller and @RestController
        for (var def : ((ConfigurableApplicationContext)this.applicationContext).findBeanDefinitions(Object.class)) {
            Class<?> beanClass = def.getBeanClass();
            Object bean = def.getRequiredInstance();
            Controller controller = beanClass.getAnnotation(Controller.class);
            RestController restController = beanClass.getAnnotation(RestController.class);
            if (controller != null && restController != null) {
                throw new ServletException("Found @Controller and @RestController on class: " + beanClass.getName());
            }
            if (controller != null) {
                addController(false, controller.value(), bean);
            }
            if (restController != null) {
                addController(true, restController.value(), bean);
            }

        }
    }

    void addController(boolean isRest, String name, Object instance) throws ServletException {
        logger.info("add {} controller '{}': {}", isRest ? "rest" : "MVC", name, instance.getClass().getName());
        addMethods(isRest, name, instance, instance.getClass());
    }

    void addMethods(boolean isRest, String name, Object instance, Class<?> type) throws ServletException {
        for (Method m : type.getDeclaredMethods()) {
            GetMapping get = m.getAnnotation(GetMapping.class);
            if (get != null) {
                checkMethod(m);
                this.getDispatchers.add(new Dispatcher("GET", isRest, instance, m, get.value()));
            }
            PostMapping post = m.getAnnotation(PostMapping.class);
            if (post != null) {
                checkMethod(m);
                this.postDispatchers.add(new Dispatcher("POST", isRest, instance, m, post.value()));
            }
        }
        Class<?> superClass = type.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            addMethods(isRest, name, instance, superClass);
        }
    }

    void checkMethod(Method m) throws ServletException {
        int mod = m.getModifiers();
        if (Modifier.isStatic(mod)) {
            throw new ServletException("Can do URL mapping to static method: " + m);
        }
        m.setAccessible(true);
    }

    @Override
    public void destroy() {
        this.applicationContext.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("{} {}", req.getMethod(), req.getRequestURI());
       String url = req.getRequestURI();
       if (url.equals(this.faviconPath) || url.startsWith(this.resourcePath)) {
           doResource(url, req, resp);
       } else {
           doService(req, resp, this.getDispatchers);
       }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp, this.postDispatchers);
    }

    void doService(HttpServletRequest req, HttpServletResponse resp, List<Dispatcher> dispatchers) throws ServletException, IOException {
        String url = req.getRequestURI();
        try {
            doService(url, req, resp, dispatchers);
        } catch (ErrorResponseException e) {
            logger.warn("process request failed with status " + e.statusCode + " : " + url, e);
            if (!resp.isCommitted()) {
                resp.resetBuffer();
                resp.sendError(e.statusCode);
            }
        } catch (RuntimeException | ServletException | IOException e) {
            logger.warn("process request failed: " + url, e);
            throw e;
        } catch (Exception e) {
            logger.warn("process request failed: " + url, e);
            throw new NestedRuntimeException(e);
        }
    }

    void doService(String url, HttpServletRequest req, HttpServletResponse resp, List<Dispatcher> dispatchers) throws Exception {
        for (Dispatcher dispatcher : dispatchers) {
            Result result = dispatcher.process(url, req, resp);
            if (result.processed) {
                Object r = result.returnObject;
                if (dispatcher.isRest) {
                    // send rest response
                    if (!resp.isCommitted()) {
                        resp.setContentType("application/json");
                    }
                    if (dispatcher.isResponseBody) {
                        if (r instanceof String s) {
                            // send as response body:
                            PrintWriter pw = resp.getWriter();
                            pw.write(s);
                            pw.flush();
                        } else if (r instanceof byte[] data) {
                            // send as response body:
                            ServletOutputStream output = resp.getOutputStream();
                            output.write(data);
                            output.flush();
                        } else {
                            // error, but i do not know why
                            throw new ServletException("Unable to process REST result when handle url: " + url);
                        }
                    } else if (!dispatcher.isVoid) {
                        PrintWriter pw = resp.getWriter();
                        JsonUtils.writeJson(pw, r);
                        pw.flush();
                    }
                } else {
                    // process MVC
                    if (!resp.isCommitted()) {
                        resp.setContentType("text/html");
                    }
                    if (r instanceof String s) {
                        if (dispatcher.isResponseBody) {
                            // send as response body:
                            PrintWriter pw = resp.getWriter();
                            pw.write(s);
                            pw.flush();
                        } else if (s.startsWith("redirect:")) {
                            resp.sendRedirect(s.substring("redirect:".length()));
                        } else {
                            // error:
                            throw new ServletException("Unable to process String result when handle url: " + url);
                        }
                    } else if (r instanceof byte[] data) {
                        if (dispatcher.isResponseBody) {
                            // send as response body:
                            ServletOutputStream output = resp.getOutputStream();
                            output.write(data);
                            output.flush();
                        } else {
                            // error:
                            throw new ServletException("Unable to process byte[] result when handle url: " + url);
                        }
                    } else if (r instanceof ModelAndView mv) {
                        String view = mv.getViewName();
                        if (view.startsWith("redirect:")) {
                            resp.sendRedirect(view.substring("redirect:".length()));
                        } else {
                            this.viewResolver.render(view, mv.getModel(), req, resp);
                        }
                    } else if (!dispatcher.isVoid && r != null) {
                        // error:
                        throw new ServletException("Unable to process" + r.getClass().getName()+ " result when handle url: " + url);
                    }
                }
                return;
            }
        }
        // not found
        resp.sendError(404, "Not Found");
    }

    void doResource(String url, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServletContext ctx = req.getServletContext();
        try (InputStream input = ctx.getResourceAsStream(url)) {
            if (input == null) {
                resp.sendError(404, "Not Found");
            } else {
                // guess content type
                String file = url;
                int n = url.lastIndexOf('/');
                if (n >= 0) {
                    file = file.substring(n + 1);
                }
                String mime = ctx.getMimeType(file);
                if (mime == null) {
                    mime = "application/octet-stream";
                }
                resp.setContentType(mime);
                ServletOutputStream output = resp.getOutputStream();
                input.transferTo( output);
                output.flush();
            }
        }
    }









    static class Dispatcher {

        final static Result NOT_PROCESSED = new Result(false, null);
        final Logger logger = LoggerFactory.getLogger(getClass());

        boolean isRest;
        boolean isResponseBody;
        boolean isVoid;
        Pattern urlPattern;
        Object controller;
        Method handlerMethod;
        Param[] methodParams;

        public Dispatcher(String httpMethod, boolean isRest, Object controller, Method method, String urlPattern) throws ServletException {
            this.isRest = isRest;
            this.isResponseBody = method.isAnnotationPresent(ResponseBody.class);
            this.isVoid = method.getReturnType() == void.class;
            this.urlPattern = PathUtils.compile(urlPattern);
            this.controller = controller;
            this.handlerMethod = method;
            Parameter[] parameters = method.getParameters();
            Annotation[][] paramsAnnos = method.getParameterAnnotations();
            this.methodParams = new Param[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                this.methodParams[i] = new Param(httpMethod, method, parameters[i], paramsAnnos[i]);
            }
            logger.atDebug().log("mapping {} to handler {}.{}", urlPattern, controller.getClass().getSimpleName(), method.getName());
            if (logger.isDebugEnabled()) {
                for (var p : this.methodParams) {
                    logger.debug("> param: {}", p);
                }

            }
        }

        Result process(String url, HttpServletRequest request, HttpServletResponse response) throws Exception {
            Matcher matcher = urlPattern.matcher(url);
            if (matcher.matches()) {
                Object[] arguments = new Object[this.methodParams.length];
                for (int i = 0; i < arguments.length; i++) {
                    Param param = this.methodParams[i];
                    arguments[i] = switch (param.paramType) {
                        case PATH_VARIABLE -> {
                            try {
                                String s = matcher.group(param.name);
                                yield convertToType(param.classType, s);
                            } catch (Exception e) {
                                throw new ServerErrorException("Cannot find path variable " + param.name);
                            }
                        }
                        case REQUEST_BODY -> {
                            BufferedReader reader = request.getReader();
                            yield JsonUtils.readJson(reader, param.classType);
                        }
                        case REQUEST_PARAM -> {
                            String s = getOrDefault(request, param.name, param.defaultValue);
                            yield convertToType(param.classType, s);
                        }
                        case SERVLET_VARIABLE -> {
                            Class<?> classType = param.classType;
                            if (classType == HttpServletRequest.class) {
                                yield request;
                            }
                            if (classType == HttpServletResponse.class) {
                                yield response;
                            }
                            if (classType == HttpSession.class) {
                                yield request.getSession();
                            }
                            if (classType == ServletContext.class) {
                                yield request.getServletContext();
                            }
                            throw new ServerErrorException("Could not determine argument type: " + classType);
                        }
                    };
                }
                Object result = null;
                try {
                    result = this.handlerMethod.invoke(this.controller, arguments);
                } catch (InvocationTargetException e) {
                    // make sure business error is thrown to user
                    Throwable t = e.getCause();
                    if (t instanceof Exception ex) {
                        throw ex;
                    }
                    throw e;
                } catch (ReflectiveOperationException e) {
                    throw new ServerErrorException(e);
                }
                return new Result(true, result);
            }
            return NOT_PROCESSED;
        }

        Object convertToType(Class<?> classType, String s) {
            if (classType == String.class) {
                return s;
            }
            if (classType == boolean.class || classType == Boolean.class) {
                return Boolean.parseBoolean(s);
            }
            if (classType == int.class || classType == Integer.class) {
                return Integer.parseInt(s);
            }
            if (classType == long.class || classType == Long.class) {
                return Long.parseLong(s);
            }
            if (classType == byte.class || classType == Byte.class) {
                return Byte.parseByte(s);
            }
            if (classType == short.class || classType == Short.class) {
                return Short.parseShort(s);
            }
            if (classType == float.class || classType == Float.class) {
                return Float.parseFloat(s);
            }
            if (classType == double.class || classType == Double.class) {
                return Double.parseDouble(s);
            }
            throw new ServerErrorException("Cannot determine argument type: " + classType);
        }

        String getOrDefault(HttpServletRequest request, String name, String defaultValue) {
            String s = request.getParameter(name);
            if (s == null) {
                if (WebUtils.DEFAULT_PARAM_VALUE.equals(defaultValue)) {
                    throw new ServerWebInputException("Request parameter '" + name + "' not found");
                }
                return defaultValue;
            }
            return s;
        }






    }



    static enum ParamType {
        PATH_VARIABLE, REQUEST_PARAM, REQUEST_BODY, SERVLET_VARIABLE;
    }

    static class Param {

        String name;
        ParamType paramType;
        Class<?> classType;
        String defaultValue;

        public Param(String httpMethod, Method method, Parameter parameter, Annotation[] annotations) throws ServletException {
            PathVariable pv = ClassUtils.getAnnotation(annotations, PathVariable.class);
            RequestParam rp = ClassUtils.getAnnotation(annotations, RequestParam.class);
            RequestBody rb = ClassUtils.getAnnotation(annotations, RequestBody.class);
            // should only have one annotation
            int total = (pv != null ? 1 : 0) + (rp != null ? 1 : 0) + (rb != null ? 1 : 0);
            if (total > 1) {
                throw new ServletException("Annotation @PathVariable, @RequestParam, @RequestBody cannot be combined at method" + method);
            }
            this.classType = parameter.getType();
            if (pv != null) {
                this.name = pv.value();
                this.paramType = ParamType.PATH_VARIABLE;
            } else if (rp != null) {
                this.name = rp.value();
                this.defaultValue = rp.defaultValue();
                this.paramType = ParamType.REQUEST_PARAM;
            } else if (rb != null) {
                this.paramType = ParamType.REQUEST_BODY;
            } else {
                this.paramType = ParamType.SERVLET_VARIABLE;
                // check servlet variable type:
                if (this.classType != HttpServletRequest.class
                        && this.classType != HttpServletResponse.class
                        && this.classType != ServletContext.class
                        && this.classType != HttpSession.class) {
                    throw new ServerErrorException("(Missing annotation?) Unsupported argument type: " + this.classType + " at methond: " + method);
                }
            }



        }

        @Override
        public String toString() {
            return String.format("Param [name=%s, paramType=%s, classType=%s, defaultValue=%s]", name, paramType, classType, defaultValue);
        }
    }



    static record Result(boolean processed, Object returnObject) {}






























}
