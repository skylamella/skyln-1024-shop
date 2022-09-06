package cn.skyln.common.utils;

import cn.skyln.common.enums.BizCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: lamella
 * @Date: 2022/08/20/12:37
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonData {

    private int code;
    private Object data;
    private String msg;

    public static JsonData returnJson(int code, String msg) {
        return new JsonData(code, null, msg);
    }

    public static JsonData returnJson(BizCodeEnum bizCodeEnum) {
        return new JsonData(bizCodeEnum.getCode(), null, bizCodeEnum.getMsg());
    }

    public static JsonData returnJson(BizCodeEnum bizCodeEnum, Object data) {
        return new JsonData(bizCodeEnum.getCode(), data, bizCodeEnum.getMsg());
    }
}
