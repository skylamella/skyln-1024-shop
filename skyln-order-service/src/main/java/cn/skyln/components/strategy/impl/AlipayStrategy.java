package cn.skyln.components.strategy.impl;

import cn.skyln.components.strategy.PayStrategy;
import cn.skyln.config.PayUrlConfig;
import cn.skyln.web.model.VO.PayInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: lamella
 * @Date: 2022/09/27/22:20
 * @Description:
 */
@Service
@Slf4j
public class AlipayStrategy implements PayStrategy {

    @Autowired
    private PayUrlConfig payUrlConfig;

    @Override
    public String unifyOrder(PayInfoVO payInfoVO) {
        return null;
    }

    @Override
    public String refund(PayInfoVO payInfoVO) {
        return PayStrategy.super.refund(payInfoVO);
    }

    @Override
    public String queryPaySuccess(PayInfoVO payInfoVO) {
        return PayStrategy.super.queryPaySuccess(payInfoVO);
    }
}
