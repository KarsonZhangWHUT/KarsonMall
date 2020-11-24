package com.karson.mall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * spu信息介绍
 * 
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-06 21:40:51
 */
@Data
@TableName("pms_spu_info_desc")
@ToString
public class SpuInfoDescEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 商品id
	 */
	@TableId(type = IdType.INPUT)
	private Long spuId;
	/**
	 * 商品介绍
	 */
	private String decript;

}
