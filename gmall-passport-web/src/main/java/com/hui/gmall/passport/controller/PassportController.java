package com.hui.gmall.passport.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PassportController {
    @RequestMapping("index")
    public String index(){
        return "index";
    }
}
