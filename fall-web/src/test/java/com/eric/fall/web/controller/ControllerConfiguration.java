package com.eric.fall.web.controller;

import com.eric.fall.annotation.ComponentScan;
import com.eric.fall.annotation.Configuration;
import com.eric.fall.annotation.Import;
import com.eric.fall.web.WebMvcConfiguration;


@Configuration
@Import(WebMvcConfiguration.class)
public class ControllerConfiguration {
}
