package cn.skyln;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author: lamella
 * @Date: 2022/09/07/21:45
 * @Description:
 */
@SpringBootApplication
@MapperScan("cn.skyln.web.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class CouponApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponApplication.class, args);
    }
}
