package com.eric.fall.jdbc.tx;

import com.eric.fall.annotation.Transactional;
import com.eric.fall.aop.AnnotationProxyBeanPostProcessor;

public class TransactionalBeanPostProcessor extends AnnotationProxyBeanPostProcessor<Transactional> {
}
