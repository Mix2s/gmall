package com.hui.gmall.service;

import com.hui.gmall.bean.PmsSearchParam;
import com.hui.gmall.bean.PmsSearchSkuInfo;

import java.io.IOException;
import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) throws IOException;
}
