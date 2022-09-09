package cn.skyln.exception;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: lamella
 * @Date: 2022/08/31/22:15
 * @Description:
 */
@ControllerAdvice
@Slf4j
public class BizExceptionHandle {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonData handler(Exception e) {
        if (e instanceof BizException) {
            BizException bizException = (BizException) e;
            log.error("【业务异常】 {}", bizException.getMsg());
            return JsonData.returnJson(bizException.getCode(), bizException.getMsg());
        } else {
            log.error("【系统异常】 {}", e.getMessage());
            return JsonData.returnJson(BizCodeEnum.SYSTEM_ERROR);
        }
    }

}
