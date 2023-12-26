package de.flexitrade.gateway.config;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
//@EnableHystrix
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GatewayConfig {

    private final MainGatewayFilter gatewayFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        final Function<GatewayFilterSpec, UriSpec> filter = f -> f.filter(gatewayFilter).retry(1);

        return builder.routes()
                .route("apiAuthenticationModule", r -> r.path("/auth/*")
                        .filters(filter)
                        .uri("lb://authentication-service"))

                .route("apiUsermanagementModule", r -> r.path("/usermanagement/*")
                        .filters(filter)
                        .uri("lb://usermanagement-service"))

                .build();
    }
}