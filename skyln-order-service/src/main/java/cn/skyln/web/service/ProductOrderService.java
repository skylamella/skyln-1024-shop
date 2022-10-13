package cn.skyln.web.service;

import cn.skyln.enums.ProductOrderPayTypeEnum;
import cn.skyln.model.OrderCloseMessage;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.DO.ProductOrderDO;
import cn.skyln.web.model.REQ.ConfirmOrderRequest;
import cn.skyln.web.model.REQ.RepayOrderRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

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

    /**
     * 未支付成功，本地取消订单
     *
     * @param outTradeNo 订单号
     */
    void cancelCloseProductOrder(String outTradeNo);

    /**
     * 支付结果回调通知
     *
     * @param payType   支付类型
     * @param paramsMap 回调参数
     * @return JsonData
     */
    JsonData handlerOrderCallbackMsg(ProductOrderPayTypeEnum payType, Map<String, String> paramsMap);

    /**
     * 分页查看订单列表
     *
     * @param page      第几页
     * @param size      一页显示几条
     * @param queryType 订单类型
     * @return Map
     */
    Map<String, Object> pageProductActivity(int page, int size, String queryType);

    /**
     * 重新支付订单
     *
     * @param repayOrderRequest 重新支付订单对象
     * @return JsonData
     */
    JsonData repayOrder(RepayOrderRequest repayOrderRequest);
}
