package com.eric.fall.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.*;
import java.util.function.Function;

public class PropertyResolver {

    Logger logger = LoggerFactory.getLogger(getClass());

    Map<String, String> properties = new HashMap<>();

    Map<Class<?>, Function<String, Object>> converters = new HashMap<>();

    public PropertyResolver(Properties props) {
        this.properties.putAll(System.getenv());
        Set<String> names = props.stringPropertyNames();
        for (String name : names) {
            this.properties.put(name, props.getProperty(name));
        }
        if (logger.isDebugEnabled()) {
            List<String> keys = new ArrayList<>(this.properties.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                logger.debug("propertyResolver: {} = {}", key, this.properties.get(key));
            }
        }

        // register converters
        converters.put(String.class, s -> s);
        converters.put(boolean.class, s -> Boolean.parseBoolean(s));
        converters.put(Boolean.class, s -> Boolean.valueOf(s));

        converters.put(byte.class, s -> Byte.parseByte(s));
        converters.put(Byte.class, s -> Byte.valueOf(s));

        converters.put(short.class, s -> Short.parseShort(s));
        converters.put(Short.class, s -> Short.valueOf(s));

        converters.put(int.class, s -> Integer.parseInt(s));
        converters.put(Integer.class, s -> Integer.valueOf(s));

        converters.put(long.class, s -> Long.parseLong(s));
        converters.put(Long.class, s -> Long.valueOf(s));

        converters.put(float.class, s -> Float.parseFloat(s));
        converters.put(Float.class, s -> Float.valueOf(s));

        converters.put(double.class, s -> Double.parseDouble(s));
        converters.put(Double.class, s -> Double.valueOf(s));

        converters.put(LocalDate.class, s -> LocalDate.parse(s));
        converters.put(LocalTime.class, s -> LocalTime.parse(s));
        converters.put(LocalDateTime.class, s -> LocalDateTime.parse(s));
        converters.put(ZonedDateTime.class, s -> ZonedDateTime.parse(s));
        converters.put(Duration.class, s -> Duration.parse(s));
        converters.put(ZoneId.class, s -> ZoneId.of(s));

    }

    public boolean containsProperty(String key) {
        return this.properties.containsKey(key);
    }

    // ${app.title:${APP_NAME:Summer}}
    public String getProperty(String key) {
        PropertyExpr expr = parsePropertyExpr(key);
        if (expr != null) {
            if (expr.defaultValue() != null) {
                return getProperty(expr.key(), expr.defaultValue());
            } else {
                return getRequiredProperty(expr.key());
            }
        }
        String value = this.properties.get(key);
        if (value != null) {
            return parserValue(value);
        }
        return value;
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : parserValue(defaultValue);

    }
    public <T> T getProperty(String key, Class<T> targetType) {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        return convert(targetType, value);
    }

    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return convert(targetType, value);
    }

    public String getRequiredProperty(String key) {
        String value = getProperty(key);
        return Objects.requireNonNull(value, "required property " + key + " is not found");
    }

    public <T> T getRequiredProperty(String key, Class<T> targetType) {
        T value = getProperty(key, targetType);
        return Objects.requireNonNull(value, "required property " + key + " is not found");
    }

    String parserValue(String value) {
        PropertyExpr expr = parsePropertyExpr(value);
        if (expr == null) {
            return value;
        }
        if (expr.defaultValue() != null) {
            return getProperty(expr.key(), expr.defaultValue());
        } else {
            return getRequiredProperty(expr.key());
        }
    }

    <T> T convert(Class<T> clazz, String value) {
        var fn = this.converters.get(clazz);
        if (fn == null) {
            throw new IllegalArgumentException("Unsupported value type " + clazz.getName());
        }
        return (T)fn.apply(value);
    }

    //${abc.xyz:defaultValue}
    PropertyExpr parsePropertyExpr(String key) {
        if (key.startsWith("${") && key.endsWith("}")) {
            int n = key.indexOf(':');
            if (n == -1) {
                // no default value: ${key}
                String k = notEmpty(key.substring(2, key.length() - 1));
                return new PropertyExpr(k, null);
            } else {
                // has defalut value: ${key:defaultValue}
                String k = notEmpty(key.substring(2, n));
                return new PropertyExpr(k, key.substring(n + 1, key.length() - 1));

            }
        }
        return null;
    }

    String notEmpty(String key) {
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key cannot be empty");
        }
        return key;
    }

}

record PropertyExpr(String key, String defaultValue) {}

