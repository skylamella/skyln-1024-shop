package cn.skyln.common.enums;

import lombok.Getter;

/**
 * @Author: lamella
 * @Date: 2022/08/31/21:22
 * @Description:
 */
public enum BizCodeEnum {
    /**
     * 成功操作码
     */
    SEARCH_SUCCESS(0,"数据查询成功！"),
    OPERATE_SUCCESS(0,"操作成功！数据更新可能延迟，如刷新后未显示变更，请耐心等待。"),
    SEND_CODE_SUCCESS(0,"验证码发送成功！"),

    /**
     * 通用操作码
     */
    OPS_REPEAT(100001, "重复操作。"),

    // 各服务枚举开始

    /**
     * 验证码
     */
    CODE_TO_ERROR(200001, "接收号码不合规。"),
    CODE_LIMITED(200002, "验证码发送过快。"),
    CODE_ERROR(200003, "验证码错误。"),
    CODE_CAPTCHA_ERROR(200101, "图形验证码错误。"),

    /**
     * 账号
     */
    ACCOUNT_REPEAT(210001, "账号已经存在。"),
    ACCOUNT_UNREGISTER(210002, "无法找到该用户，请联系系统管理员。"),
    ACCOUNT_PWD_ERROR(210003, "账号或密码错误，请重试，或联系系统管理员。"),
    ACCOUNT_PERMISSION_ERROR(210004, "您无权访问该系统资源，请联系系统管理员。"),

    // 各服务枚举结束

    // 系统枚举开始

    /**
     * 系统
     */
    SYSTEM_ERROR(300001, "系统错误，请稍后重试，或联系系统管理员。"),
    SYSTEM_NO_NACOS_INSTANCE(300002, "没有NACOS服务，请稍后重试，或联系系统管理员。"),

    /**
     * 数据
     */
    DATA_SPECIFICATION(310001, "数据不符合规范，请检查后重新输入。"),
    ERROR_DATA(310002, "错误数据"),
    NO_DATA(310003, "数据不存在，请检查后重新输入。"),

    /**
     * 流控
     */
    FLOW_EXCEPTION(320001, "已触发限流，请稍后再次尝试。"),
    DEGRADE_EXCEPTION(320002, "已触发降级，请稍后再次尝试。"),
    PARAM_FLOW_EXCEPTION(320003, "热点参数异常，已触发流量控制，请稍后再次尝试。"),
    SYSTEM_BLOCK_EXCEPTION(320004, "系统规则异常，已触发流量控制，请稍后再次尝试。"),
    AUTHORITY_EXCEPTION(320005, "认证异常，已触发流量控制，请稍后再次尝试。");

    // 系统枚举结束

    @Getter
    private int code;
    @Getter
    private String msg;

    private BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
