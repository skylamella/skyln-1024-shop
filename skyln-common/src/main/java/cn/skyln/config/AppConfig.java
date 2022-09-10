package cn.skyln.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: lamella
 * @Date: 2022/09/10/11:23
 * @Description:
 */
@Configuration
@Data
public class AppConfig {
    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private String redisPort;
    @Value("${spring.redis.password}")
    private String redisPwd;

    /**
     * 配置分布式锁客户端
     * @return
     */
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        //单机模式
        config.useSingleServer()
                .setPassword(redisPwd)
                .setAddress("redis://"+redisHost+":"+redisPort);
        //集群模式
//        config.useClusterServers()
//                .setScanInterval(2000)
//                .addNodeAddress("redis://10.0.29.30:6379", "redis://10.0.29.95:6379");
        return Redisson.create(config);
    }
}
