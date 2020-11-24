package com.karson.mall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Karson
 */
@Data
public class PurchaseDoneVo {
    @NotNull
    private Long id;//采购单ID

    private List<PurchaseItemDoneVo> items;

}
