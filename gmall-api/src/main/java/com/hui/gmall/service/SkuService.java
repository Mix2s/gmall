package com.hui.gmall.service;

import com.hui.gmall.bean.PmsSkuInfo;

import java.util.List;

public interface SkuService {
     void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuById(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);
}
