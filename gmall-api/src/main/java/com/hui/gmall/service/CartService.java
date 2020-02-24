package com.hui.gmall.service;

import com.hui.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {

    OmsCartItem ifCartExistByUser(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItemFormDb);

    void flushCartCache(String memberId);

    List<OmsCartItem> cartList(String userId);

    void checkCart(OmsCartItem omsCartItem);
}
