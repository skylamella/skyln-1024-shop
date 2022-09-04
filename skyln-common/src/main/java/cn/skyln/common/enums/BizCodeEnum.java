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
    LOGIN_SUCCESS(0,"登录成功！"),

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
    CODE_NOT_EXIST_ERROR(200004, "验证码不存在。"),
    CODE_CAPTCHA_ERROR(200101, "图形验证码错误。"),

    /**
     * 账号
     */
    ACCOUNT_REPEAT(210001, "账号已经存在。"),
    ACCOUNT_LOGIN_ERROR(210002, "账号或密码错误，请重试，或联系系统管理员。"),
    ACCOUNT_PERMISSION_ERROR(210003, "您无权访问该系统资源，请联系系统管理员。"),
    ACCOUNT_REGISTER_SUCCESS(210004, "账号注册成功！"),
    ACCOUNT_REGISTER_PWD_ERROR(210005, "两次输入的密码不同，请检查后再次输入。"),
    ACCOUNT_REGISTER_ERROR(210006, "账号注册失败。"),
    ACCOUNT_NOT_EXIST_ERROR(210007, "输入的账号为空。"),
    ACCOUNT_PWD_NOT_EXIST_ERROR(210008, "输入的密码为空。"),
    ACCOUNT_UNLOGIN_ERROR(210009, "登录状态失效或当前未登录，请重新登录。"),
    ACCOUNT_ACCESS_TOKEN_EXPIRED(210010, "当前token过期但不需要重新登录。"),
    ACCOUNT_ACCESS_TOKEN_EXPIRED_RELOGIN(210011, "当前token过期且需要重新登录。"),
    ACCOUNT_SAFE_MODE_RELOGIN(210012, "两次登录的IP地址不同，请重新登录。"),

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
     * 文件相关
     */
    FILE_UPLOAD_USER_IMG_FAIL(310101, "用户头像上传失败。"),

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
