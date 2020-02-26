package com.hui.gmall.passport.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.hui.gmall.bean.UmsMember;
import com.hui.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token){
        //通过jwt校验真假
        return "success";
    }


    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember){
        //调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);
        if(umsMemberLogin!=null){
            //登陆成功
        }else{
            //登录失败
        }
        return "token";
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap map){
        map.put("ReturnUrl",ReturnUrl);
        return "index";
    }
}
