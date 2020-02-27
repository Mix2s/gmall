package com.hui.gmall.passport.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.hui.gmall.bean.UmsMember;
import com.hui.gmall.service.UserService;
import com.hui.gmall.util.HttpclientUtil;
import com.hui.gmall.utils.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {


    @Reference
    UserService userService;


    @RequestMapping("vlogin")
    public String vlogin(String code, HttpServletRequest request) {
        //client_id=2717789182
        //App Secret：fb47ba860e61a7bf196718d3f44d641c
        //http://passport.gmall.com:8085/vlogin?code=4db7136ae2831be0925ed6bd05cee523
        //授权码换取access_token
        String s3 = "https://api.weibo.com/oauth2/access_token";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id", "2717789182");
        paramMap.put("client_secret", "fb47ba860e61a7bf196718d3f44d641c");
        paramMap.put("grant_type", "authorization_code");
        paramMap.put("redirect_uri", "http://passport.gmall.com:8085/vlogin");
        paramMap.put("code", code);

        String access_token_json = HttpclientUtil.doPost(s3, paramMap);
        Map<String, Object> access_map = JSON.parseObject(access_token_json, Map.class);
        //access_token 换取用户信息
        String uid = (String) access_map.get("uid");
        String access_token = (String) access_map.get("access_token");

        String showUser_url = "https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + uid;
        String user_json = HttpclientUtil.doGet(showUser_url);
        Map<String, Object> user_map = JSON.parseObject(user_json, Map.class);

        //将用户信息保存数据库 用户类型设置微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType("2");  //设置微博用户 来源id
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid((String) user_map.get("idstr"));
        umsMember.setCity((String) user_map.get("location"));
        umsMember.setNickname((String) user_map.get("screen_name"));

        String g = "2";
        String gender = (String) user_map.get("gender");
        if (gender.equals("m")) {
            g = "1";
        }
        if (gender.equals("f")) {
            g = "0";
        }
        umsMember.setGender(g);

        UmsMember umsCheck = new UmsMember();
        umsCheck.setSourceUid(umsMember.getSourceUid());
        UmsMember umsMemberCheck = userService.checkOauthUser(umsCheck);
        if (umsMemberCheck == null) {
            //之前未保存 保存umsMemberId
            userService.addOauthUser(umsMember);
        } else {
            umsMember = umsMemberCheck;
        }

        //生成jwt 的token 并且重定向到首页 携带该token

        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();
        String token = makeToken(memberId,nickname,request);

        //将token存入redis一份
        userService.addUserToken(token, memberId);

        return "redirect:http://search.gmall.com:8083/index?token="+token;
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token, String currentIp) {
        //通过jwt校验真假
        Map<String, String> map = new HashMap<>();

        Map<String, Object> decode = JwtUtil.decode(token, "2020gmallAhui", currentIp);
        if (decode != null) {
            map.put("status", "success");
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickname", (String) decode.get("nickname"));

        } else {
            map.put("status", "fail");
        }

        return JSON.toJSONString(map);
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

            token = makeToken(memberId,nickname,request);

            //将token存入redis一份
            userService.addUserToken(token, memberId);


        } else {
            //登录失败
            token = "fail";
        }
        return token;
    }


    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap map) {
        map.put("ReturnUrl", ReturnUrl);
        return "index";
    }

    public String makeToken(String memberId, String nickname, HttpServletRequest request) {
        String token = "";
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
        return token;
    }
}
