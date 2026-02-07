package com.eric.fall.web;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface View {

    default String getContentType() {
        return null;
    }

    void render(@Nullable Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception;















}
