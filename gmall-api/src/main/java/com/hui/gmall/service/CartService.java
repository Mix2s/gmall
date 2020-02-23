package com.hui.gmall.service;

import com.hui.gmall.bean.OmsCartItem;

public interface CartService {

    OmsCartItem ifCartExistByUser(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItemFormDb);

    void flushCartCache(String memberId);
}
