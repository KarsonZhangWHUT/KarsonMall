package com.karson.mall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Karson
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;

}
