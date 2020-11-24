package com.karson.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.karson.common.to.es.SkuESModel;
import com.karson.mall.search.constant.ESConstant;
import com.karson.mall.search.config.MallElasticSearchConfig;
import com.karson.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Karson
 */
@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException {
        //保存到es中
        //在es中建立索引，product，建立好映射关系


        //给es中保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuESModel model : skuESModels) {
            //构造保存请求
            IndexRequest indexRequest = new IndexRequest(ESConstant.PRODUCT_INDEX);
            indexRequest.id(model.getSkuId().toString());
            String string = JSON.toJSONString(model);
            indexRequest.source(string, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, MallElasticSearchConfig.COMMON_OPTIONS);
        //TODO 如果批量错误，可以处理
        boolean hasFailures = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
        log.info("商品上架完成：{}", collect);
        return !hasFailures;
    }
}
