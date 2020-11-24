package com.karson.mall.ware.vo;

import lombok.Data;

/**
 * @author Karson
 */
@Data
public class PurchaseItemDoneVo {
    private Long itemId;
    private Integer status;
    private String reason;
}
