package com.karson.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Karson
 * catalogId=225&keyword=小米&sort=saleCount_asc&hasStock=0/1
 * &skuPrice=1_500/_500/500_&brandId=1&brandId=2
 * &attrs=1_系统:安卓&attrs=2_屏幕尺寸:3.0英寸以下
 * &pageNum=2
 */
@Data
public class SearchParam {
    private String keyword;
    private Long catalog3Id;
    //排序条件
    /* sort=saleCount_asc/desc
     * sort=skuPrice_desc/asc
     * sort=hotSore=asc/desc
     */
    private String sort;

    //过滤条件
    private Integer hasStock;
    //skuPrice=1_500/_500/500_
    private String skuPrice;
    //brandId=1&brandId=2,允许多选
    private List<Long> brandId;
    private List<String> attrs;//按照属性筛选
    private Integer pageNum = 1;

    private String _queryString;//原生的查询条件
}
