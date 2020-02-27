package com.hui.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hui.gmall.util.HttpclientUtil;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.HashMap;
import java.util.Map;

public class TestOauth2 {

    public static void main(String[] args) {
        getCode();
    }
    //得到授权码
    public static String getCode(){  //
        // App Key：2717789182
        // App Secret：fb47ba860e61a7bf196718d3f44d641c
        // http://passport.gmall.com:8085/vlogin
        String s = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=2717789182&response_type=code&redirect_uri=http://passport.gmall.com:8085/vlogin");
        System.out.println(s);
        String s1 = "http://passport.gmall.com:8085/vlogin?code=1fd72d23076aedff2b38f58fb8f4ec48";
        return null;
    }

    public static String getAccessToken(){
        String s3 = "https://api.weibo.com/oauth2/access_token"; //?client_id=2717789182&client_secret=fb47ba860e61a7bf196718d3f44d641c&grant_type=authorization_code&redirect_uri=http://passport.gmall.com:8085/vlogin&code=CODE"

        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","2717789182");
        paramMap.put("client_secret","fb47ba860e61a7bf196718d3f44d641c");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.gmall.com:8085/vlogin");
        paramMap.put("code","7c845b0b6e2c7b5e0d1241c292cd4de7");

        String access_token_json = HttpclientUtil.doPost(s3, paramMap);
        Map<String,String> access_map = JSON.parseObject(access_token_json,Map.class);

        System.out.println(access_map.get("access_token"));
        return access_map.get("access_token");
    }
    public static Map<String,String> getUser_Info(){
        //用access_token 查询用户信息2.0055s_ZG5HZvxC27396226d7XwLT8C
        String s4 = "https://api.weibo.com/2/users/show.json?access_token=2.0055s_ZG5HZvxC27396226d7XwLT8C&uid=1";
        String user_json = HttpclientUtil.doGet(s4);
        Map<String,String> user_Map = JSON.parseObject(user_json, Map.class);
        System.out.println(user_Map.get("1"));
        return user_Map;
    }
}
