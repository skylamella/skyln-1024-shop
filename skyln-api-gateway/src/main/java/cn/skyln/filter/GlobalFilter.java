package cn.skyln.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Author: lamella
 * @Date: 2022/10/06/12:51
 * @Description:
 */
@Component
@Slf4j
public class GlobalFilter implements org.springframework.cloud.gateway.filter.GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("request.getId()={}", request.getId());
        log.info("request.getURI()={}", request.getURI());
        log.info("request.getMethodValue()={}", request.getMethodValue());
        RequestPath path = request.getPath();
        log.info("path.contextPath().value()={}", path.contextPath().value());
        log.info("path.pathWithinApplication().value()={}", path.pathWithinApplication().value());
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
