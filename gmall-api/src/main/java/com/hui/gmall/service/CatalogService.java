package com.hui.gmall.service;

import com.hui.gmall.bean.PmsBaseCatalog1;
import com.hui.gmall.bean.PmsBaseCatalog2;
import com.hui.gmall.bean.PmsBaseCatalog3;

import java.util.List;

public interface CatalogService {
    List<PmsBaseCatalog1> getCatalog1();

    List<PmsBaseCatalog3> getCatalog3(String catalog2Id);

    List<PmsBaseCatalog2> getCatalog2(String catalogId);
}
