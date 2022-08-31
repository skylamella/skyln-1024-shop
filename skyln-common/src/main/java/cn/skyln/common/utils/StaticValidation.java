package cn.skyln.common.utils;

import java.util.regex.Pattern;

/**
 * @Author: lamella
 * @Date: 2022/08/20/15:00
 * @Description:
 */
public class StaticValidation {

    public static boolean stringIsNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
}
