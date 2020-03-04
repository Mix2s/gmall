package com.hui.gmall.payment.mq;

import com.hui.gmall.bean.PaymentInfo;
import com.hui.gmall.payment.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PamentServiceMqListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumePaymentCheckResult(MapMessage mapMessage) {

        String out_trade_no = null;
        Integer count = 0;

        try {
            out_trade_no = mapMessage.getString("out_trade_no");
            if(mapMessage.getString("count")!=null){
                count = mapMessage.getInt("count");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }


        //调用pamentService的支付宝接口
        Map<String, Object> resultMap = paymentService.checkAlipayPayment(out_trade_no);

        if (resultMap != null && !resultMap.isEmpty()) {
            String trade_status = (String)resultMap.get("trade_status");

            //根据查询支付状态 判断是否进行下次延迟任务还是支付成功更新数据和后续任务
            if (StringUtils.isNotBlank(trade_status)&&trade_status.equals("TRADE_SUCCESS")) {

                //支付成功 更新支付发送支付队列
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setAlipayTradeNo((String)resultMap.get("trade_no"));// 支付宝的交易凭证号
                paymentInfo.setCallbackContent((String)resultMap.get("call_back_content"));//回调请求字符串
                paymentInfo.setCallbackTime(new Date());

                //支付成功 更新支付发送支付队列
                System.out.println("支付成功");
                paymentService.updatePayment(paymentInfo);
                return;
            }
        }

        if (count > 0) {
            System.out.println("监测剩余次数"+count);
            count--;
            paymentService.sendDelayPaymentResultCheckQueue(out_trade_no, count);
        } else {
            System.out.println("结束监听");
        }

    }
}

