package com.hui.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hui.gmall.bean.PaymentInfo;
import com.hui.gmall.mq.ActiveMQUtil;
import com.hui.gmall.payment.service.PaymentService;
import com.hui.gmall.payment.service.mapper.PaymentInfoMapper;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insert(paymentInfo);
    }


    @Override
    public void updatePayment(PaymentInfo paymentInfo) {

        String orderSn = paymentInfo.getOrderSn();
        Example e = new Example(PaymentInfo.class);
        e.createCriteria().andEqualTo("orderSn",orderSn);

        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException ex) {
            ex.printStackTrace();
        }

        try {
            paymentInfoMapper.updateByExampleSelective(paymentInfo,e);
            //支付成功订单服务更新

            //调用mq发送支付成功消息
            Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);

            //ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage(); //字符文本
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage(); //hash结构
            mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());

            producer.send(mapMessage);

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
