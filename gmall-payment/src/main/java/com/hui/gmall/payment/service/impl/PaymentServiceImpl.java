package com.hui.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hui.gmall.bean.PaymentInfo;
import com.hui.gmall.payment.service.PaymentService;
import com.hui.gmall.payment.service.mapper.PaymentInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

@Service
public class PaymentServiceImpl implements PaymentService {
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
        paymentInfoMapper.updateByExampleSelective(paymentInfo,e);
    }
}
