package cn.skyln.constant;

/**
 * @Author: lamella
 * @Date: 2022/09/01/21:55
 * @Description:
 */
public class CacheKey {
    /**
     * %s是占位符，第一个是类型，第二个是接收号码
     */
    public static final String CHECK_CODE_KEY = "code:%s:%s";

    /**
     * %s是占位符，第一个是业务类型，第二个是内容ID
     */
    public static final String DISTRIBUTED_LOCK_KEY = "lock:%s:%s";
}
