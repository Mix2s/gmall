package com.hui.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;

import com.hui.gmall.bean.OmsOrder;
import com.hui.gmall.bean.OmsOrderItem;
import com.hui.gmall.order.mapper.OmsOrderItemMapper;
import com.hui.gmall.order.mapper.OmsOrderMapper;
import com.hui.gmall.service.CartService;
import com.hui.gmall.service.OrderService;
import com.hui.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Reference
    CartService cartService;

    // User:memberId:tradeCode

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = redisUtil.getJedis();

        String tradeKey = "user:"+memberId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString(); //生成随机字符串

        jedis.setex(tradeKey,60*15,tradeCode);

        jedis.close();
        return tradeCode;
    }

    @Override
    public String checkTradeCode(String memberId,String tradeCode) {
        Jedis jedis = redisUtil.getJedis();

        try {
            String tradeKey = "user:" + memberId + ":tradeCode";

            String tradeCodeFromCache = jedis.get(tradeKey);

            //对比防重删令牌
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey),
                    Collections.singletonList(tradeCode));


            if (eval!=null&&eval!=0) {  //保持一致 删除redis
                // jedis.del(tradeKey);
                return "success";
            }else{
                return "fail";
            }
        }finally {
            jedis.close();
        }
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {

        //保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();

        //保存订单详情
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //删除购物车数据
            //cartService.delCart();
        }
    }

}
