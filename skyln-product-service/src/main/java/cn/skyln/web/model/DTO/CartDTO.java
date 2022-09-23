package cn.skyln.web.model.DTO;

import lombok.Data;

import java.util.List;

/**
 * @Author: lamella
 * @Date: 2022/09/23/22:41
 * @Description:
 */
@Data
public class CartDTO {
    /**
     * 订单号
     */
    private String orderOutTradeNo;
    /**
     * 商品ID列表
     */
    private List<Long> productIdList;
}
