package cn.skyln.enums;

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
    SEARCH_SUCCESS(0,"数据查询成功"),
    OPERATE_SUCCESS(0,"操作成功！数据更新可能延迟，如刷新后未显示变更，请耐心等待"),
    SEND_CODE_SUCCESS(0,"验证码发送成功"),
    LOGIN_SUCCESS(0,"登录成功"),

    /**
     * 通用操作码
     */
    OPS_REPEAT(100001, "重复操作"),

    // 各服务枚举开始

    /**
     * 验证码
     */
    CODE_TO_ERROR(200001, "接收号码不合规"),
    CODE_LIMITED(200002, "验证码发送过快"),
    CODE_ERROR(200003, "验证码错误"),
    CODE_NOT_EXIST_ERROR(200004, "验证码不存在"),
    CODE_CAPTCHA_ERROR(200101, "图形验证码错误"),

    /**
     * 账号
     */
    ACCOUNT_REPEAT(210001, "账号已经存在"),
    ACCOUNT_LOGIN_ERROR(210002, "账号或密码错误，请重试，或联系系统管理员"),
    ACCOUNT_PERMISSION_ERROR(210003, "您无权访问该系统资源，请联系系统管理员"),
    ACCOUNT_REGISTER_SUCCESS(210004, "账号注册成功"),
    ACCOUNT_REGISTER_PWD_ERROR(210005, "两次输入的密码不同，请检查后再次输入"),
    ACCOUNT_REGISTER_ERROR(210006, "账号注册失败"),
    ACCOUNT_NOT_EXIST_ERROR(210007, "输入的账号为空"),
    ACCOUNT_PWD_NOT_EXIST_ERROR(210008, "输入的密码为空"),
    ACCOUNT_UNLOGIN_ERROR(210009, "登录状态失效或当前未登录，请重新登录"),
    ACCOUNT_ACCESS_TOKEN_EXPIRED(210010, "当前token过期但不需要重新登录"),
    ACCOUNT_ACCESS_TOKEN_EXPIRED_RELOGIN(210011, "当前token过期且需要重新登录"),
    ACCOUNT_SAFE_MODE_RELOGIN(210012, "两次登录的IP地址不同，请重新登录"),

    /**
     * 地址相关
     */
    ADDRESS_NOT_EXIT(211001, "收货地址不存在"),
    ADDRESS_ADD_FAIL(211002,"新增收货地址失败"),
    ADDRESS_DEL_FAIL(211003,"删除收货地址失败"),
    ADDRESS_UPD_FAIL(211004,"更新收货地址失败"),

    /**
     * 优惠券
     */
    COUPON_CONDITION_ERROR(220001,"优惠券条件错误"),
    COUPON_UNAVAILABLE(220002,"没有可用的优惠券"),
    COUPON_NO_EXITS(220003,"优惠券不存在"),
    COUPON_NO_STOCK(220005,"优惠券库存不足"),
    COUPON_OUT_OF_LIMIT(220006,"优惠券领取超过限制次数"),
    COUPON_OUT_OF_TIME(220007,"优惠券不在领取时间范围"),
    COUPON_GET_FAIL(220008,"优惠券领取失败"),
    COUPON_RECORD_LOCK_FAIL(220009,"优惠券锁定失败"),

    /**
     * 订单
     */
    ORDER_CONFIRM_COUPON_FAIL(230001,"创建订单-优惠券使用失败,不满足价格条件"),
    ORDER_CONFIRM_PRICE_FAIL(230002,"创建订单-验价失败"),
    ORDER_CONFIRM_LOCK_PRODUCT_FAIL(230003,"创建订单-商品库存不足锁定失败"),
    ORDER_CONFIRM_ADD_STOCK_TASK_FAIL(230004,"创建订单-新增商品库存锁定任务"),
    ORDER_CONFIRM_TOKEN_NOT_EXIST(230008,"订单令牌缺少"),
    ORDER_CONFIRM_TOKEN_EQUAL_FAIL(230009,"订单令牌不正确"),
    ORDER_CONFIRM_NOT_EXIST(230010,"订单不存在"),
    ORDER_CONFIRM_CART_ITEM_NOT_EXIST(230011,"购物车商品项不存在"),

    /**
     * 支付
     */
    PAY_ORDER_FAIL(240001,"创建支付订单失败"),
    PAY_ORDER_CALLBACK_SIGN_FAIL(240002,"支付订单回调验证签失败"),
    PAY_ORDER_CALLBACK_NOT_SUCCESS(240003,"创建支付订单失败"),
    PAY_ORDER_NOT_EXIST(240005,"订单不存在"),
    PAY_ORDER_STATE_ERROR(240006,"订单状态不正常"),
    PAY_ORDER_PAY_TIMEOUT(240007,"订单支付超时"),

    /**
     * 轮播图相关
     */
    BANNER_NOT_EXIT(250001, "轮播图不存在"),
    BANNER_ADD_FAIL(250002,"新增轮播图失败"),
    BANNER_DEL_FAIL(250003,"删除轮播图失败"),
    BANNER_UPD_FAIL(250004,"更新轮播图失败"),

    /**
     * 商品相关
     */
    PRODUCT_NOT_EXIT(251001, "商品不存在"),
    PRODUCT_ADD_FAIL(251002,"新增商品失败"),
    PRODUCT_DEL_FAIL(251003,"删除商品失败"),
    PRODUCT_UPD_FAIL(251004,"更新商品失败"),

    /**
     * 购物车相关
     */
    CART_NOT_EXIT(252001, "购物车中没有商品"),
    CART_UPD_NUM_FAIL(252002,"请输入大于0的商品数量"),

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
    DATA_ERROR(310002, "错误数据"),
    DATA_NOT_EXIST(310003, "数据不存在，请检查后重新输入。"),

    /**
     * 文件相关
     */
    FILE_UPLOAD_USER_IMG_FAIL(311001, "用户头像上传失败。"),

    /**
     * 流控
     */
    CONTROL_FLOW_EXCEPTION(320001, "已触发限流，请稍后再次尝试。"),
    CONTROL_DEGRADE_EXCEPTION(320002, "已触发降级，请稍后再次尝试。"),
    CONTROL_PARAM_FLOW_EXCEPTION(320003, "热点参数异常，已触发流量控制，请稍后再次尝试。"),
    CONTROL_SYSTEM_BLOCK_EXCEPTION(320004, "系统规则异常，已触发流量控制，请稍后再次尝试。"),
    CONTROL_AUTHORITY_EXCEPTION(320005, "认证异常，已触发流量控制，请稍后再次尝试。");

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
