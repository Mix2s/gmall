package com.hui.gmall.service;

import com.hui.gmall.bean.PmsBaseAttrInfo;
import com.hui.gmall.bean.PmsBaseAttrValue;
import com.hui.gmall.bean.PmsBaseSaleAttr;

import java.util.List;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    List<PmsBaseSaleAttr> baseSaleAttrList();
}
