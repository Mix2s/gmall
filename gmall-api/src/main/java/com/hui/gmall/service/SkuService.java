package com.hui.gmall.service;

import com.hui.gmall.bean.PmsSkuInfo;

import java.util.List;

public interface SkuService {
     void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    PmsSkuInfo getSkuById(String skuId,String ip);

    List<PmsSkuInfo> getAllSku();
}
