package cn.skyln.common.utils;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @Author: lamella
 * @Date: 2022/08/20/16:02
 * @Description:
 */
public class CommonUtils {
    private static final String ALL_CHAR_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * MD5加密字符串
     * @param oldStr 待加密字符串
     * @return 加密后字符串
     */
    public static String MD5(String oldStr) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(oldStr.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }

            return sb.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取完整URL
     * @param request HttpServletRequest
     * @return 完整URL
     */
    public static String getFullUrl(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (!StringUtils.isEmpty(queryString)) {
            requestURL = requestURL + "?" + queryString;
        }
        return requestURL;
    }

    /**
     * 获取一个随机整数
     * @param maxNum 随机整数边界
     * @return 随机整数
     */
    public static int getRandomNum(int maxNum) {
        if (maxNum == 1) {
            return 0;
        } else {
            Random random = new Random();
            int temp = random.nextInt(maxNum);
            if (temp > 0 && temp == maxNum) {
                return temp - 1;
            } else {
                return temp;
            }
        }
    }

    /**
     * 获取访问用户的IP地址
     * @param request HttpServletRequest
     * @return 访问用户的IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    ipAddress = InetAddress.getLocalHost().getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) {
                // "***.***.***.***".length()
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }

    /**
     * 生成随机字符串
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String getRandomCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(ALL_CHAR_NUM.charAt(getRandomNum(ALL_CHAR_NUM.length() - 1)));
        }
        return sb.toString();
    }

    public static long getCurrentTimeStamp(){
        return System.currentTimeMillis();
    }

    /**
     * 向前台发送json字符串
     *
     * @param response HttpServletResponse
     * @param json     转化后的json字符串
     */
    public static void renderJson(HttpServletResponse response, Object json) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(JSON.toJSON(json));
            response.flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
