package com.hui.gmall.cart.controller;

import java.math.BigDecimal;

public class TestBigDecimal {

    public static void main(String[] args) {
        //初始化
        BigDecimal b1 = new BigDecimal(0.01f);
        BigDecimal b2 = new BigDecimal(0.01d);
        BigDecimal b3 = new BigDecimal("0.01");
        BigDecimal b4 = new BigDecimal(2);
        BigDecimal b5 = new BigDecimal(4);
        System.out.println(b1);
        System.out.println(b2);
        System.out.println(b3);

        //比较
        int i = b1.compareTo(b2);
        System.out.println(i);

        //运算
        BigDecimal add = b1.add(b2);
        System.out.println(add);

        BigDecimal subtract = b1.subtract(b2);
        System.out.println(subtract);

        BigDecimal multiply = b1.multiply(b2);
        System.out.println(multiply);

        BigDecimal divide = b1.divide(b2,BigDecimal.ROUND_DOWN);
        System.out.println(divide);

        //约数
    }
}
