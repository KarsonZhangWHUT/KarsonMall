package com.karson.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Karson
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;

}
