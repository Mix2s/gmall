package com.hui.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hui.gmall.bean.*;
import com.hui.gmall.service.AttrService;
import com.hui.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.*;

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
        map.put("skuLsInfoList", pmsSearchSkuInfos);

        //抽取检索结果包含属性集合
        Set<String> valueIdSet = new HashSet<>();

        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();

            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }

        //根据valueId 将属性列表查询出
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        map.put("attrList", pmsBaseAttrInfos);

        //对平台属性进一步处理 去掉当前条件中valueId 属性组
        String[] delValueIds = pmsSearchParam.getValueId();
        if(delValueIds!=null){
            Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
            while (iterator.hasNext()){
                PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                    String valueId = pmsBaseAttrValue.getId();
                    for (String delValueId : delValueIds) {
                        if(delValueId.equals(valueId)){
                            //删除
                            iterator.remove();
                        }
                    }
                }
            }
        }

        String urlParam = getUrlParam(pmsSearchParam);
        map.put("urlParam",urlParam);
        String keyword = pmsSearchParam.getKeyword();
        if(StringUtils.isNotBlank(keyword)){
            map.put("keyword",keyword);
        }


        //面包屑
        List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
        map.put("attrValueSelectedList",pmsSearchCrumbs);

        return "list";
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam) {
       String keyword = pmsSearchParam.getKeyword();
       String catalog3Id = pmsSearchParam.getCatalog3Id();
       String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam=urlParam+"&";
            }
            urlParam=urlParam+"&keyword="+keyword;
        }
        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam=urlParam+"&";
            }
            urlParam=urlParam+"&catalog3Id="+catalog3Id;
        }
        if(skuAttrValueList!=null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                urlParam=urlParam+"&valueId="+pmsSkuAttrValue;
            }
        }
        return urlParam;
    }

    @RequestMapping("index")
    public String index() {
        return "index";
    }
}
