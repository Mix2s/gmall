package com.hui.gmall.interceptors;

import com.hui.gmall.annotations.LoginRequired;
import com.hui.gmall.utils.CookieUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //拦截器代码实现
        //1 判断被拦截的请求的访问方法的注解（是否需要拦截） 反射
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);

        if(methodAnnotation==null){
            //通过拦截器
            return true;
        }

        boolean loginSuccess = methodAnnotation.loginSuccess(); //获取请求必须登陆成功
        System.out.println("进入拦截器方法");
        return true;
    }
}