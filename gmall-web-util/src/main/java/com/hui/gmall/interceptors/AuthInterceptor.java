package com.hui.gmall.interceptors;

import com.hui.gmall.annotations.LoginRequired;
import com.hui.gmall.util.HttpclientUtil;
import com.hui.gmall.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    /*
        拦截器
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //拦截器代码实现
        //1 判断被拦截的请求的访问方法的注解（是否需要拦截） 反射
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);

        //是否拦截
        if (methodAnnotation == null) {
            //通过拦截器
            return true;
        }

        String token = "";

        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }
        //已经拦截是否登录
        boolean loginSuccess = methodAnnotation.loginSuccess(); //获取请求必须登陆成功

        //验证过程 调用认证中心进行验证
        String success = "fail";
        if(StringUtils.isNotBlank(token)){
            success = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token);
        }

        if (loginSuccess) {
            //必须登陆成功
            if (!success.equals("success")) {
                //重定向passport登录
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl="+requestURL);
                return false;
            } else {
                //验证通过 覆盖cookie中的token
                //需要token用户信息写入
                request.setAttribute("memberId", "1");
                request.setAttribute("nickname", "ahui");
                return true;
            }
        } else {
            // 不要登录也能用 必须验证
            if (success.equals("success")) {
                //需要token用户信息写入
                request.setAttribute("memberId", "1");
                request.setAttribute("nickname", "ahui");
            }
        }

        //验证通过 覆盖cookie中的token
        if(StringUtils.isNotBlank(token)){
            CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
        }
        return true;
    }
}