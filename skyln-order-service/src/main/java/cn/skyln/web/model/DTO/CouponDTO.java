package cn.skyln.web.model.DTO;

import lombok.Data;

import java.util.List;

/**
 * @Author: lamella
 * @Date: 2022/09/24/17:41
 * @Description:
 */
@Data
public class CouponDTO {

    /**
     * 订单号
     */
    private String orderOutTradeNo;
    /**
     * 优惠券ID列表
     */
    private List<Long> couponRecordIdList;
}
