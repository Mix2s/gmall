package com.hui.gmall.service;

import com.hui.gmall.bean.PmsBaseAttrInfo;

import java.util.List;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);
}
