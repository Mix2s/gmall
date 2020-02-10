package com.hui.gmall.manage.controller;

import org.csource.fastdfs.ClientGlobal;

public class TestFdfs {

    public static void main(String[] args) {

        String originalFilename = "123123.123123.123123.12313.jpg";
        int i = originalFilename.lastIndexOf(".");
        String extName = originalFilename.substring(i+1);
        System.out.println(extName);

    }
}
