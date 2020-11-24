package com.karson.mall.search.vo;

import com.karson.common.to.es.SkuESModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karson
 */
@Data
public class SearchResult {

    //查询到的所有商品信息
    private List<SkuESModel> products;

    /*
     * 分页信息
     */
    private Integer pageNum;
    private Long total;
    private Integer totalPages;
    private List<Integer> pageNavs;

    //当前查询到的结果，所有涉及到的品牌
    private List<BrandVo> brands;

    private List<CatalogVo> catalogs;

    //当前查询所有涉及到的属性
    private List<AttrVo> attrs;

    //面包屑导航
    private List<NavVo> navs = new ArrayList<>();

    private List<Long> attIds = new ArrayList<>();


    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
