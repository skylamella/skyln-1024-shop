package cn.skyln.exception;

import cn.skyln.enums.BizCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: lamella
 * @Date: 2022/08/31/22:14
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BizException extends RuntimeException {
    private Integer code;
    private String msg;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.msg = message;
    }

    public BizException(BizCodeEnum bizCodeEnum) {
        super(bizCodeEnum.getMsg());
        this.code = bizCodeEnum.getCode();
        this.msg = bizCodeEnum.getMsg();
    }
}
