package com.hui.gmall.order.mq;

import com.hui.gmall.bean.OmsOrder;
import com.hui.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Controller;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Controller
public class OrderServiceMqListener {

    @Autowired
    OrderService orderService;


    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage){
        String out_trade_no;
        try {
            out_trade_no = mapMessage.getString("out_trade_no");
            //更新订单状态

            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setOrderSn(out_trade_no);
            orderService.updateOrder(omsOrder);

            //发送一个订单已经支付队列
            System.out.println("111");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
