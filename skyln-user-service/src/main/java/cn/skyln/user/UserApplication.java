package cn.skyln.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: lamella
 * @Date: 2022/08/30/22:38
 * @Description:
 */
@SpringBootApplication
@MapperScan("cn.skyln.user.web.mapper")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
