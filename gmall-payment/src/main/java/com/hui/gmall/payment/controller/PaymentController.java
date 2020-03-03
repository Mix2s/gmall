package com.hui.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.hui.gmall.annotations.LoginRequired;
import com.hui.gmall.bean.OmsOrder;
import com.hui.gmall.bean.PaymentInfo;
import com.hui.gmall.payment.config.AlipayConfig;
import com.hui.gmall.payment.service.PaymentService;
import com.hui.gmall.service.OrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {
    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentService paymentService;

    @Reference
    OrderService orderService;

    @RequestMapping("alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public String aliPayCallBackReturn(HttpServletRequest request,ModelMap modelMap){

        //回调请求中获取支付宝参数
        // 回调请求中获取支付宝参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();

        //通过支付宝paramMap进行签名验证 2.0接口paramMap参数去掉 同步失效
        if(StringUtils.isNotBlank(sign)){
            // 验签成功
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);// 支付宝的交易凭证号
            paymentInfo.setCallbackContent(call_back_content);//回调请求字符串
            paymentInfo.setCallbackTime(new Date());
            // 更新用户的支付状态
            paymentService.updatePayment(paymentInfo);
        }
        //支付成功订单服务更新

        //调用mq发送支付成功消息

        return "finish";
    }

    @RequestMapping("alipay/submit")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public String alipay(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request){
        //获得支付宝请求客户端（封装好的http请求）
       String form = null;
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        Map<String,Object> map = new HashMap<>();

        //回调函数地址
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        map.put("out_trade_no",outTradeNo);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",0.01);
        map.put("subject","小米MIX致尚之白测试");
        String param = JSON.toJSONString(map);
        alipayRequest.setBizContent(param);
        try{
            form = alipayClient.pageExecute(alipayRequest).getBody();
        }catch (AlipayApiException e){
            e.printStackTrace();
        }
        //生成保存用户信息
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("光辉商城");
        paymentInfo.setTotalAmount(totalAmount);

        paymentService.savePaymentInfo(paymentInfo);

        //请求到支付宝
        return form;
    }

    @RequestMapping("weixin/submit")
    @LoginRequired(loginSuccess = true)
    public String weixin(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request){

        return null;
    }

    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap) {
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        modelMap.put("totalAmount",totalAmount);
        modelMap.put("nickname",nickname);
        modelMap.put("outTradeNo",outTradeNo);
        return "index";
    }
}
