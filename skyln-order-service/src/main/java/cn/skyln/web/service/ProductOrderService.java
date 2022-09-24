package cn.skyln.web.service;

import cn.skyln.model.OrderCloseMessage;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.DO.ProductOrderDO;
import cn.skyln.web.model.REQ.ConfirmOrderRequest;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author skylamella
 * @since 2022-09-12
 */
public interface ProductOrderService extends IService<ProductOrderDO> {

    /**
     * 创建订单
     *
     * @param confirmOrderRequest 确认订单对象
     * @return JsonData
     */
    JsonData confirmOrder(ConfirmOrderRequest confirmOrderRequest);

    /**
     * 查询订单状态
     *
     * @param outTradeNo 订单号
     * @return 订单状态
     */
    String queryProductOrderState(String outTradeNo);

    /**
     * 延迟自动关单
     *
     * @param orderCloseMessage MQ消息体
     * @return 关单结果
     */
    boolean delayCloseProductOrder(OrderCloseMessage orderCloseMessage);
}
