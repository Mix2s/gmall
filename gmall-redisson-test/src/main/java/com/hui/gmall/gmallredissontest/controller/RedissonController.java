package com.hui.gmall.gmallredissontest.controller;

import com.hui.gmall.util.RedisUtil;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

@Controller
public class RedissonController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("test")
    @ResponseBody
    public String testRedisson(){
        RLock rLock = redissonClient.getLock("lock");
        Jedis jedis = redisUtil.getJedis();
        //lock
        rLock.lock();
        try {
            String v = jedis.get("k");
            if (StringUtils.isBlank(v)) {
                v = "1";
            }
            System.out.println("-->" + v);
            jedis.set("k", (Integer.parseInt(v) + 1) + "");
        }finally {
            jedis.close();
            rLock.unlock();
        }
        return "success";
    }
}
