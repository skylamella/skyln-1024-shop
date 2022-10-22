package cn.skyln.constant;

/**
 * @Author: lamella
 * @Date: 2022/09/01/21:55
 * @Description:
 */
public class CacheKey {
    /**
     * 注册验证码key，%s是占位符，第一个是类型，第二个是接收号码
     */
    public static final String CHECK_CODE_KEY = "code:%s:%s";

    /**
     * 分部署锁key，%s是占位符，第一个是业务类型，第二个是内容ID
     */
    public static final String DISTRIBUTED_LOCK_KEY = "lock:%s:%s";

    /**
     * 购物车key，%s是占位符，是用户ID
     */
    public static final String CART_KEY = "cart:%s";

    /**
     * MQ缓存key，%s是占位符，第一个是业务类型，第二个是内容ID
     */
    public static final String MQ_KEY = "mq:%s:%s";

    /**
     * 提交表单的token key，%s是占位符，第一个是用户ID
     */
    public static final String SUBMIT_ORDER_TOKEN_KEY = "order:submit:%s";
}
