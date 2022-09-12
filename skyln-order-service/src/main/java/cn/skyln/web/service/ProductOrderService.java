package cn.skyln.web.service;

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
}
