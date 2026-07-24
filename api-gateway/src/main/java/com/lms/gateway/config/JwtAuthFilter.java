package com.lms.gateway.config;

import com.lms.security.JwtProvider;
import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtProvider jwtProvider;

    public JwtAuthFilter(JwtProvider jwtProvider) {
        super(Config.class);
        this.jwtProvider = jwtProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization Header Format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                if (!jwtProvider.validateToken(token)) {
                    return onError(exchange, "Invalid or Expired JWT Token", HttpStatus.UNAUTHORIZED);
                }

                String email = jwtProvider.getEmailFromToken(token);
                Long id = jwtProvider.getIdFromToken(token);
                request = request.mutate()
                        .headers(httpHeaders -> httpHeaders.remove("X-User-Email"))
                        .headers(httpHeaders -> httpHeaders.remove("X-User-Id"))
                        .header("X-User-Email", email)
                        .header("X-User-Id", String.valueOf(id))
                        .build();
            } catch (Exception e) {
                return onError(exchange, "JWT Validation Failed", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        System.out.println("[JwtAuthFilter ERROR]: " + err);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    @Data
    public static class Config {}
}
