package com.hui.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hui.gmall.annotations.LoginRequired;
import com.hui.gmall.bean.*;
import com.hui.gmall.service.CartService;
import com.hui.gmall.service.OrderService;
import com.hui.gmall.service.SkuService;
import com.hui.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    //可以用来存放交易码
    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;

    /*
        订单服务
     */
    @RequestMapping("submitOrder")
    @LoginRequired(loginSuccess = true)
    public String submitOrder(String receiveAddressId, BigDecimal totalAmount, String tradeCode, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //检验唯一交易吗
        String success = orderService.checkTradeCode(memberId, tradeCode);
        if (success.equals("success")) {
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            // 订单对象
            OmsOrder omsOrder = new OmsOrder();

            omsOrder.setAutoConfirmDay(7);  //确认收货时间
            omsOrder.setCreateTime(new Date());  //订单创建时间
            omsOrder.setDiscountAmount(null);  //总价格
            //omsOrder.setFreightAmount();
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            // 订单外部编号
            String outTradeNo = "gmall";
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHmmss");
            String format = sdf.format(new Date());  //时间字符串 当前毫秒时间戳
            outTradeNo = outTradeNo+System.currentTimeMillis()+format;


            omsOrder.setNote("订单备注");
            omsOrder.setOrderSn(outTradeNo);
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);  //订单类型

            //收货人信息
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressId(receiveAddressId);
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());

            //获取收货时间 当前时间加一天
            Calendar calender = Calendar.getInstance();  //获取当前时间
            calender.add(Calendar.DATE,1);
            Date time = calender.getTime();
            omsOrder.setReceiveTime(time);
            omsOrder.setSourceType(0);
            omsOrder.setStatus(0);  //订单状态
            omsOrder.setTotalAmount(totalAmount);

            //根据用户 id 获取要购买的商品列表（购物车）
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId); //获取一个购物车集

            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    //订单详情列表
                    OmsOrderItem omsOrderItem = new OmsOrderItem();

                    //检验价格
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if(b==false){
                        return "tradeFail";
                    }
                    //检验库存 远程调用库存系统
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());

                    omsOrderItem.setOrderSn(outTradeNo);   //外部订单号 用来对接外部支付系统

                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());  //订单三级id
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("11111111");  //商品条形码
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSn("仓库编号");  //在仓库中的skuid

                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);

            //将订单和订单详情写入数据库
            //删除购物车对应的商品
            orderService.saveOrder(omsOrder);
            //重定向到支付系统


        } else {
            return "tradeFail";
        }
        return null;
    }

    /*
        结算页面
     */
    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        //http://192.168.159.134/group1/M00/00/00/wKifhl5Fc92AOpAQAADyc2RCwm4049.jpg

        //收件人地址列表
        List<UmsMemberReceiveAddress> receiveAddressByMemberId = userService.getReceiveAddressByMemberId(memberId);

        //将购物车集合转化展示给页面  userAddressList consignee  userAddress
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

        List<OmsOrderItem> omsOrderItems = new ArrayList<>();

        for (OmsCartItem omsCartItem : omsCartItems) {
            if (omsCartItem.getIsChecked().equals("1")) {
                //循环购物车对象封装一个商品到orderItem
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());

                omsOrderItems.add(omsOrderItem);
            }
        }

        modelMap.put("omsOrderItems", omsOrderItems);
        modelMap.put("userAddressList", receiveAddressByMemberId);
        modelMap.put("totalAmount", getTotalAmount(omsCartItems));

        //生成唯一交易码 提交订单时，进行交易码的校验  通过memberId来确保同一个用户
        String tradeCode = orderService.genTradeCode(memberId);
        modelMap.put("tradeCode", tradeCode);
        return "trade";
    }

    //计算总金额
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();

            if (omsCartItem.getIsChecked().equals("1")) {
                totalAmount = totalAmount.add(totalPrice);
            }
        }

        return totalAmount;
    }
}
