package com.hui.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hui.gmall.bean.PmsSearchParam;
import com.hui.gmall.bean.PmsSearchSkuInfo;
import com.hui.gmall.bean.PmsSkuAttrValue;
import com.hui.gmall.bean.PmsSkuInfo;
import com.hui.gmall.service.AttrService;
import com.hui.gmall.service.SearchService;
import com.hui.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class searchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String index(PmsSearchParam pmsSearchParam, ModelMap map) throws IOException {  //三级分类id 关键字 平台属性集合
        //调用搜索服务返回搜索结果
       List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);
       map.put("skuLsInfoList",pmsSearchSkuInfos);

       //抽取检索结果包含属性集合
        Set<String> valueIdSet = new HashSet<>();

        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();

            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }

        //根据valueId 将属性列表查询出来

       return "list";
    }

    @RequestMapping("index")
    public String index(){
        return "index";
    }
}
