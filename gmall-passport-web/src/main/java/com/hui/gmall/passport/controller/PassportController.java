package com.hui.gmall.passport.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.hui.gmall.bean.UmsMember;
import com.hui.gmall.service.UserService;
import com.hui.gmall.utils.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.naming.ldap.HasControls;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token) {
        //通过jwt校验真假
        return "success";
    }


    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        String token = "";
        //调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);
        if (umsMemberLogin != null) {
            //登陆成功

            //用jwt制作token
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("memberId", memberId);
            userMap.put("nickname", nickname);

            String ip = request.getHeader("x-forwarded-for");   //获通过nginx转发的客户端ip

            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();  //没有从nginx获取ip 说明没有从nginx做代理 直接从http中获取
            }
            if (StringUtils.isBlank(ip)) { //当前 ip没有值 说明nginx代理失败 http获取失败
                ip = "127.0.0.1";  //这里定位本机
            }

            // 建议对参数进行加密 例如 MD5
            token = JwtUtil.encode("2020gmallAhui", userMap, ip);  //ip过短会造成 盐值重复

            //将token存入redis一份
            userService.addUserToken(token,memberId);


        } else {
            //登录失败
            token = "fail";
        }
        return "token";
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap map) {
        map.put("ReturnUrl", ReturnUrl);
        return "index";
    }
}
