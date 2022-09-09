package cn.skyln.utils;

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
     * @param str 待判断字符串
     * @return 待判断字符串是否是数字
     */
    public static boolean stringIsNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
}
