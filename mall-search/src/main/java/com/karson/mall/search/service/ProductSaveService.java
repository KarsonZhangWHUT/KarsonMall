package com.karson.mall.search.service;

import com.karson.common.to.es.SkuESModel;

import java.io.IOException;
import java.util.List;

/**
 * @author Karson
 */
public interface ProductSaveService {

    boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException;
}
