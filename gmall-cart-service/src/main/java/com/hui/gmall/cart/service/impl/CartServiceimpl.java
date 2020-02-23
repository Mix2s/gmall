package com.hui.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hui.gmall.bean.OmsCartItem;
import com.hui.gmall.cart.mapper.OmsCartItemMapper;
import com.hui.gmall.service.CartService;
import com.hui.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class CartServiceimpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem omsCartItem1 = omsCartItemMapper.selectOne(omsCartItem);
        return omsCartItem1;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        /*
            只处理了当前数据存在
            TDDD 当前数据不存在
         */
        if(StringUtils.isNotBlank(omsCartItem.getMemberId())){
            omsCartItemMapper.insert(omsCartItem);
        }
    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFormDb) {
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id",omsCartItemFormDb.getId());

        omsCartItemMapper.updateByExampleSelective(omsCartItemFormDb,e);
    }

    @Override
    public void flushCartCache(String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> select = omsCartItemMapper.select(omsCartItem);

        //同步到 cache中
        Jedis jedis = redisUtil.getJedis();

    }
}
