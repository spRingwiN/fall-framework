package com.eric.fall.metric;

import com.eric.fall.annotation.Component;
import com.eric.fall.aop.AnnotationProxyBeanPostProcessor;

@Component
public class MetricProxyBeanPostProcessor extends AnnotationProxyBeanPostProcessor<Metric> {
}
