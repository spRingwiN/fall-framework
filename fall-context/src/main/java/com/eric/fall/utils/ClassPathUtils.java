package com.eric.fall.utils;

import com.eric.fall.io.InputStreamCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class ClassPathUtils {

    static final Logger LOGGER = LoggerFactory.getLogger(ClassPathUtils.class);

    public static <T> T readInputStream(String path, InputStreamCallback<T> inputStreamCallback) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        try (InputStream input = getContextClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new FileNotFoundException("file not found: " + path);
            }
            return inputStreamCallback.doWithInputStream(input);
        } catch (IOException e) {
            LOGGER.error("read input stream error", e);
            throw new UncheckedIOException(e);
        }
    }

    public static String readString(String path) {
        return readInputStream(path, input -> {
            byte[] data = input.readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        });
    }


    static ClassLoader getContextClassLoader() {
        ClassLoader cl = null;
        cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassPathUtils.class.getClassLoader();

        }
        return cl;
    }
















}
