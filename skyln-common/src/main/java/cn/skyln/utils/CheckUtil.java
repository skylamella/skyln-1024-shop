package cn.skyln.utils;

import cn.skyln.constant.CacheKey;
import cn.skyln.enums.BizCodeEnum;
import cn.skyln.exception.BizException;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: lamella
 * @Date: 2022/09/02/21:44
 * @Description:
 */
public class CheckUtil {

    /**
     * 邮箱正则
     */
    private static final Pattern MAIL_PATTERN = Pattern.compile("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
    /**
     * 手机号正则，暂时未用
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");

    /**
     * @param email 邮箱号
     * @return 是否是邮箱
     */
    public static boolean isEmail(String email) {
        if (null == email || "".equals(email)) {
            return false;
        }
        Matcher m = MAIL_PATTERN.matcher(email);
        return m.matches();
    }

    /**
     * @param phone 手机号
     * @return 是否是手机号
     */
    public static boolean isPhone(String phone) {
        if (null == phone || "".equals(phone)) {
            return false;
        }
        Matcher m = PHONE_PATTERN.matcher(phone);
        return m.matches();
    }

    /**
     * 判断一个字符串是否是数字
     *
     * @param str 待判断字符串
     * @return 待判断字符串是否是数字
     */
    public static boolean stringIsNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 通用性MQ消费次数判定
     *
     * @param redisTemplate redisTemplate
     * @param log           sl4j日志
     * @param mqKey         mqKey
     * @param msg           msg
     * @param msgTag        msgTag
     * @param channel       channel
     * @param serviceName   serviceName
     * @return 重试次数
     */
    public static int checkMQRetryNums(RedisTemplate redisTemplate,
                                       Logger log,
                                       String mqKey,
                                       Object msg,
                                       long msgTag,
                                       Channel channel,
                                       String serviceName) throws IOException {
        int retryNums = 1;
        if (redisTemplate.hasKey(mqKey)) {
            retryNums = (int) redisTemplate.opsForValue().get(mqKey);
            redisTemplate.delete(mqKey);
            if (retryNums < 5) {
                redisTemplate.opsForValue().set(mqKey, ++retryNums);
                log.error("{}-失败，第{}次重试：{}", serviceName, retryNums, msg);
                channel.basicReject(msgTag, true);
            } else {
                log.error("{}-失败，重试次数超过5次：{}", serviceName, msg);
                // 重试次数超过5次，确认消息消费成功
                channel.basicAck(msgTag, false);
            }
        } else {
            log.error("{}-失败，第1次重试：{}", serviceName, msg);
            redisTemplate.opsForValue().set(mqKey, 1);
            channel.basicReject(msgTag, true);
        }
        return retryNums;
    }

    /**
     * 防重下单token校验
     *
     * @param orderToken    下单token
     * @param log           sl4j日志
     * @param redisTemplate redisTemplate
     * @param loginUserId   登录用户ID
     */
    public static void checkOrderToken(String orderToken,
                                       Logger log,
                                       RedisTemplate redisTemplate,
                                       Long loginUserId) {
        if (StringUtils.isBlank(orderToken)) {
            log.error("用户下单令牌不存在");
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_TOKEN_NOT_EXIST);
        }
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String key = String.format(CacheKey.SUBMIT_ORDER_TOKEN_KEY, loginUserId);
        Long result = (Long) redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), List.of(key), orderToken);
        assert result != null;
        if (result == 0L) {
            log.error("用户下单订单令牌不正确：{}", orderToken);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_TOKEN_EQUAL_FAIL);
        }
    }
}
