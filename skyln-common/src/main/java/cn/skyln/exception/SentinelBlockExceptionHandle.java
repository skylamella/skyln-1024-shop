package cn.skyln.exception;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.CommonUtils;
import cn.skyln.utils.JsonData;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: lamella
 * @Date: 2022/11/06/11:40
 * @Description:
 */
@Component
public class SentinelBlockExceptionHandle implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        JsonData jsonData = null;
        if (e instanceof FlowException) {
            //限流异常
            jsonData = JsonData.returnJson(BizCodeEnum.CONTROL_FLOW_EXCEPTION);
        } else if (e instanceof DegradeException) {
            //降级异常
            jsonData = JsonData.returnJson(BizCodeEnum.CONTROL_DEGRADE_EXCEPTION);
        } else if (e instanceof ParamFlowException) {
            //参数限流异常
            jsonData = JsonData.returnJson(BizCodeEnum.CONTROL_PARAM_FLOW_EXCEPTION);
        } else if (e instanceof SystemBlockException) {
            //系统负载异常
            jsonData = JsonData.returnJson(BizCodeEnum.CONTROL_SYSTEM_BLOCK_EXCEPTION);
        } else if (e instanceof AuthorityException) {
            //授权异常
            jsonData = JsonData.returnJson(BizCodeEnum.CONTROL_AUTHORITY_EXCEPTION);
        }
        response.setStatus(200);
        CommonUtils.renderJson(response, jsonData);
    }
}
