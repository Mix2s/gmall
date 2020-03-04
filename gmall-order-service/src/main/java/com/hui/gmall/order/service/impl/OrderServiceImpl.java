package com.hui.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hui.gmall.bean.OmsOrder;
import com.hui.gmall.bean.OmsOrderItem;
import com.hui.gmall.mq.ActiveMQUtil;
import com.hui.gmall.order.mapper.OmsOrderItemMapper;
import com.hui.gmall.order.mapper.OmsOrderMapper;
import com.hui.gmall.service.CartService;
import com.hui.gmall.service.OrderService;
import com.hui.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
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

    @Autowired
    ActiveMQUtil activeMQUtil;

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
                    Collections.singletonList(tradeCode));    //放置并发条件下 一个订单多次操作

            if (eval!=null&&eval!=0) {  //保持一致 删除redis
                //jedis.del(tradeKey);
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

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example e = new Example(OmsOrder.class);
        e.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());

        OmsOrder omsOrderUpdate = new OmsOrder();

        omsOrderUpdate.setStatus("1");

        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true,Session.SESSION_TRANSACTED);
            //发送一个订单已支付的队列 提供库存消费
            Queue payment_success_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);

            //ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage(); //字符文本
            //ActiveMQMapMessage mapMessage = new ActiveMQMapMessage(); //hash结构
            TextMessage textMessage = new ActiveMQTextMessage();

            //mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());

            //查询订单对象转化字符串 存入ORDER_PAY_QUEUE消息队列
            OmsOrder omsOrderParam = new OmsOrder();
            omsOrderParam.setOrderSn(omsOrder.getOrderSn());
            OmsOrder omsOrderResponse = omsOrderMapper.selectOne(omsOrderParam);

            OmsOrderItem omsOrderItemParam = new OmsOrderItem();
            omsOrderItemParam.setProductSn(omsOrderItemParam.getOrderSn());
            List<OmsOrderItem> select = omsOrderItemMapper.select(omsOrderItemParam);

            omsOrderResponse.setOmsOrderItems(select);
            textMessage.setText(JSON.toJSONString(omsOrderResponse));

            omsOrderMapper.updateByExampleSelective(omsOrderUpdate,e);
            producer.send(textMessage);

            session.commit();
        }catch (Exception ee){
            //消息回滚
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }finally {
            try {
                connection.close();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }
    }

}
