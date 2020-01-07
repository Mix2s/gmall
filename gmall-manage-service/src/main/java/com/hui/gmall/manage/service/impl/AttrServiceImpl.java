package com.hui.gmall.manage.service.impl;



import com.alibaba.dubbo.config.annotation.Service;
import com.hui.gmall.bean.PmsBaseAttrInfo;
import com.hui.gmall.bean.PmsBaseAttrValue;
import com.hui.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.hui.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.hui.gmall.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;

    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
       PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
       pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        return pmsBaseAttrInfos;
    }
}
