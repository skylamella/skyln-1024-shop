package cn.skyln.utils;

import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lamella
 * @Date: 2022/09/10/17:39
 * @Description:
 */
public class CouponUtils {

    public static Map<String, Object> getReturnPageMap(long total, long pages, List<Object> collect) {
        Map<String, Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record", total);
        pageMap.put("total_page", pages);
        pageMap.put("current_data", collect);
        return pageMap;
    }

    public static Object beanProcess(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
