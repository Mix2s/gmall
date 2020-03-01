package com.hui.gmall.service;

import com.hui.gmall.bean.OmsOrder;

import java.math.BigDecimal;

public interface OrderService {
    String genTradeCode(String memberId);

    String checkTradeCode(String memberId,String tradeCode);

    void saveOrder(OmsOrder omsOrder);
}
