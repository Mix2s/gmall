package com.hui.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hui.gmall.annotations.LoginRequired;
import com.hui.gmall.bean.OmsCartItem;
import com.hui.gmall.bean.OmsOrderItem;
import com.hui.gmall.bean.UmsMember;
import com.hui.gmall.bean.UmsMemberReceiveAddress;
import com.hui.gmall.service.CartService;
import com.hui.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;


@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        //http://192.168.159.134/group1/M00/00/00/wKifhl5Fc92AOpAQAADyc2RCwm4049.jpg

        //收件人地址列表
        List<UmsMemberReceiveAddress> receiveAddressByMemberId = userService.getReceiveAddressByMemberId(memberId);

        //将购物车集合转化展示给页面  userAddressList consignee  userAddress
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

        List<OmsOrderItem> omsOrderItems = new ArrayList<>();

        for (OmsCartItem omsCartItem : omsCartItems) {
            if(omsCartItem.getIsChecked().equals("1")){
                //循环购物车对象封装一个商品到orderItem
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());

                omsOrderItems.add(omsOrderItem);
            }
        }

        modelMap.put("omsOrderItems",omsOrderItems);
        return "trade";
    }
}
