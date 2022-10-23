package cn.skyln.config;

import cn.skyln.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: lamella
 * @Date: 2022/09/05/21:27
 * @Description:
 */
@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //登录拦截器
        registry.addInterceptor(loginInterceptor())
                // 拦截路径
                .addPathPatterns(getAddPathPatternsList())
                // 放行路径
                .excludePathPatterns(getExcludePathPatternsList());

        WebMvcConfigurer.super.addInterceptors(registry);
    }

    @Bean
    LoginInterceptor loginInterceptor(){
        return new LoginInterceptor();
    }

    /**
     * 配置拦截器拦截接口
     * @return 拦截器拦截接口
     */
    private List<String> getAddPathPatternsList(){
        List<String> addPathPatternsList = new ArrayList<>();
        addPathPatternsList.add("/api/*/banner/**");
        addPathPatternsList.add("/api/*/product/**");
        addPathPatternsList.add("/api/*/cart/**");
        return addPathPatternsList;
    }

    /**
     * 配置拦截器放行接口
     * @return 拦截器放行接口
     */
    private List<String> getExcludePathPatternsList(){
        List<String> excludePathPatternsList = new ArrayList<>();
        // 分页查询优惠券接口
        excludePathPatternsList.add("/api/*/banner/banner_list");
        // 分页查询商品列表接口
        excludePathPatternsList.add("/api/*/product/page_product");
        // 查询商品详情接口
        excludePathPatternsList.add("/api/*/product/detail/*");
        // XXL-JOB-RPC-定时将redis中的购物车存入数据库
        excludePathPatternsList.add("/api/*/cart/redis_cart_to_mysql");
        return excludePathPatternsList;
    }
}
