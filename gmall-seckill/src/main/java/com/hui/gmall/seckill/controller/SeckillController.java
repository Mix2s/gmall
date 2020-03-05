package com.hui.gmall.seckill.controller;

import com.hui.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("kill2")
    @ResponseBody
    public String kill2() {
        Jedis jedis = redisUtil.getJedis();
        Integer stock = Integer.parseInt(jedis.get("114"));
        RSemaphore semaphore = redissonClient.getSemaphore("kill2");
        boolean b = semaphore.tryAcquire();
        if(b){
            System.out.println("当前库存剩余"+stock+"用户抢购成功 剩余人数"+(1000-stock));
        }else{
            System.out.println("当前库存剩余"+stock+"用户抢购失败");
        }
        jedis.close();
        return "1";
    }


    @RequestMapping("kill")
    @ResponseBody
    public String kill(){
        String memberId = "1";
        Jedis jedis = redisUtil.getJedis();

        //开启商品监控
        jedis.watch("114");
        Integer stock = Integer.parseInt(jedis.get("114"));
        if(stock>0){
            Transaction multi = jedis.multi();
            multi.incrBy("114",-1);
            List<Object> exec = multi.exec();
            if(exec!=null&&exec.size()>0){
                System.out.println("当前库存剩余"+stock+"用户抢购成功 剩余人数"+(1000-stock));
            }else{
                System.out.println("当前库存剩余"+stock+"用户抢购失败");
            }
        }
        jedis.close();
        return "1";
    }
}
