package cn.skyln.common.utils;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * @Author: lamella
 * @Date: 2022/08/20/16:02
 * @Description:
 */
public class CommonUtils {

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

    public static String getFullUrl(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (!StringUtils.isEmpty(queryString)) {
            requestURL = requestURL + "?" + queryString;
        }
        return requestURL;
    }

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

    public static String randomString(List<String> list) {
        Map<String, String> map = new HashMap<>();
        int num = 0;
        for (String str : list) {
            String replace = str.replace(" ", "");
            while (replace.length() > 5) {
                map.put(String.valueOf(num), replace.substring(0, 5));
                replace = replace.substring(5);
                num++;
            }
        }
        StringBuilder sb = new StringBuilder();
        int temp = getRandomNum(map.size());
        while (map.size() > 0) {
            if (map.containsKey(String.valueOf(temp))) {
                sb.append(map.get(String.valueOf(temp)));
                map.remove(String.valueOf(temp));
            }
            if (map.size() == 1) {
                Set<String> strings = map.keySet();
                for(String str : strings){
                    sb.append(map.get(str));
                    map.remove(str);
                }
            } else {
                temp = getRandomNum(map.size());
            }
            if (map.size() < 10) {
                Object[] objects = map.keySet().toArray();
                for (int i = 0; i < objects.length; i++) {
                    if (i == temp) {
                        temp = Integer.parseInt(String.valueOf(objects[i]));
                        break;
                    }
                }
            }
        }
        return sb.toString();
    }

}
