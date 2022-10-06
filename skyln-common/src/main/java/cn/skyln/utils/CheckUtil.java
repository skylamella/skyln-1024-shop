package cn.skyln.utils;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
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
     * 通用性MQ消息消费catch模块
     *
     * @param redisTemplate redisTemplate
     * @param mqKey         mqKey
     * @param msgTag        msgTag
     * @param channel       channel
     * @return 重试次数
     */
    public static int removeMQRedisKey(RedisTemplate redisTemplate,
                                       String mqKey,
                                       long msgTag,
                                       Channel channel) throws IOException {
        int retryNums = 1;
        if (redisTemplate.hasKey(mqKey)) {
            retryNums = (int) redisTemplate.opsForValue().get(mqKey);
            if (retryNums >= 5) {
                redisTemplate.delete(mqKey);
                channel.basicAck(msgTag, false);
            } else {
                channel.basicReject(msgTag, true);
            }
        }
        return retryNums;
    }
}
