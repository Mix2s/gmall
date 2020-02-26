package com.hui.gmall.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestJwt {
    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("memberId","1");
        map.put("nickname","zhangsan");
        String ip = "192.168.0.1";
        String time = new SimpleDateFormat("yyyyMMdd").format(new Date());

        String encode = JwtUtil.encode("2020gmall", map, ip + time);
        System.out.println(encode);
    }

}
