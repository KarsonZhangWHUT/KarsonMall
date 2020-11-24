package com.karson.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Karson
 */
@Data
public class SpuBoundsTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
