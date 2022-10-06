package cn.skyln.web.service.impl;

import cn.skyln.web.mapper.MqErrorLogMapper;
import cn.skyln.web.model.MqErrorLogDO;
import cn.skyln.web.service.MqErrorLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: lamella
 * @Date: 2022/10/06/13:58
 * @Description:
 */
@Service
public class MqErrorLogServiceImpl implements MqErrorLogService {
    @Autowired
    private MqErrorLogMapper mqErrorLogMapper;

    @Override
    public void insertMqErrorLog(String outTradeNo) {
        MqErrorLogDO mqErrorLogDO = new MqErrorLogDO();
        mqErrorLogDO.setOutTradeNo(outTradeNo);
        mqErrorLogMapper.insert(mqErrorLogDO);
    }
}
