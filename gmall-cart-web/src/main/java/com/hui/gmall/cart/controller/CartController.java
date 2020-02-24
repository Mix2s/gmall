package com.hui.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.hui.gmall.bean.OmsCartItem;
import com.hui.gmall.bean.PmsSkuInfo;
import com.hui.gmall.service.CartService;
import com.hui.gmall.service.SkuService;
import com.hui.gmall.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    @RequestMapping("checkCart")
    public String checkCart(String isChecked,String skuId,HttpServletRequest request, HttpServletResponse response,ModelMap modelMap){
       String memberId = "1";
        //调用服务修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setProductSkuId(skuId);

        cartService.checkCart(omsCartItem);

        //将最新的数据从缓存中查出 渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

        modelMap.put("cartList",omsCartItems);
        return "cartListInner";
    }

    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){

        List<OmsCartItem> omsCartItems = new ArrayList<>();

        String memberId = "1";

        if(StringUtils.isNotBlank(memberId)){

            //已经登陆查询db
            omsCartItems = cartService.cartList(memberId);
        }else{
            //未登录查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookie)){
                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
            }
        }
        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }
        modelMap.put("cartList",omsCartItems);
        return "cartList";
    }


    @RequestMapping("addToCart")
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response){

        List<OmsCartItem> omsCartItems = new ArrayList<>();

        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId,"");

        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getProductId());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("条形码暂无");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));


        //判断用户是否登录
        String memberId = "1"; //用户是否登录 等级

        /*
            购物车姓名
            DB： cartListDb
            Cookie:  cartListcookie
            Redis:  cartListCache
         */
        if(StringUtils.isBlank(memberId)){
            //用户没有登录

            //cookie中原有购物车数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);

            //判断原有中是否存在cookie
            if(StringUtils.isBlank(cartListCookie)){

                //原cookie中无值 cookie为null
                omsCartItems.add(omsCartItem);

            }else{  //cookie中有值

                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //判断添加的购物车数据在cookie中书否存在
                boolean exist =  if_cart_exist(omsCartItems,omsCartItem);

                if(exist){
                    //之前添加过 更新购物车数量
                    for (OmsCartItem cartItem : omsCartItems) {
                        if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                           // cartItem.setPrice(cartItem.getPrice().add(omsCartItem.getPrice()));
                        }
                    }

                }else {
                    //之前未添加新增购物车
                    omsCartItems.add(omsCartItem);
                }
                CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(omsCartItems),60*60*72,true);
            }
        }else{

            //从DB中查询
            OmsCartItem omsCartItemFormDb = cartService.ifCartExistByUser(memberId,skuId);

            if(omsCartItemFormDb==null){
                //用户没有添加当前商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("ahui");
                omsCartItem.setQuantity(new BigDecimal(quantity));
                cartService.addCart(omsCartItem);
            }else{
                //用户cookie中存在当前商品
                omsCartItemFormDb.setQuantity(omsCartItemFormDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFormDb);
            }

            //同步缓存
            cartService.flushCartCache(memberId);
        }

        return "redirect:/success.html";
    }

    private boolean if_cart_exist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b = false;
        for (OmsCartItem cartItem : omsCartItems) {
            String productSkuId = cartItem.getProductSkuId();

            if(productSkuId.equals(omsCartItem.getProductSkuId())){
                b = true;
            }
        }

        return b;
    }


}
