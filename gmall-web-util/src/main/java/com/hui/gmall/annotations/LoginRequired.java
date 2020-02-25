package com.hui.gmall.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 注解类型的 拦截器 自定义注解
@Target(ElementType.METHOD)  //只在方法有效
@Retention(RetentionPolicy.RUNTIME)  //虚拟机运行有效
public @interface LoginRequired {
    boolean loginSuccess() default true;
}
