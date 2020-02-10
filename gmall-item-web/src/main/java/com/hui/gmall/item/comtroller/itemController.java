package com.hui.gmall.item.comtroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class itemController {

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId){

        return "item";
    }

    @RequestMapping("index")
    public String index(ModelMap modelMap){

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5 ; i++) {
            list.add("循环试验"+i);
        }

        modelMap.put("check","1");
        modelMap.put("list",list);
        modelMap.put("hello","hello thymeleaf !!");
        return "index";
    }
}
