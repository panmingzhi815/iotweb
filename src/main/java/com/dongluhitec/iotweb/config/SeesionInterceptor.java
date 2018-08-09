package com.dongluhitec.iotweb.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

public class SeesionInterceptor implements HandlerInterceptor{

    private final static Logger LOGGER = LoggerFactory.getLogger(SeesionInterceptor.class);

    @Pointcut("@annotation(com.dongluhitec.iotweb.controller.*)") // 3：该注解声明切入点
    public void annotationPointCut() {}

    @Before("execution(* com.dongluhitec.iotweb.controller.*.*(..))") //6:直接拦截方法名
    public void before(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        System.out.println("方法规则式拦截,"+method.getName());

    }
}
