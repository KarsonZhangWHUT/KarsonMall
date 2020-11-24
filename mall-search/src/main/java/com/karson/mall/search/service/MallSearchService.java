package com.karson.mall.search.service;

import com.karson.mall.search.vo.SearchParam;
import com.karson.mall.search.vo.SearchResult;

/**
 * @author Karson
 */
public interface MallSearchService {

    /**
     * @param param 检索的所有参数
     * @return 返回检索的结果
     */
    SearchResult search(SearchParam param);
}
