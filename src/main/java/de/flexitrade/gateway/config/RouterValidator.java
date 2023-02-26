package de.flexitrade.gateway.config;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
public class RouterValidator {

    public static final List<String> openPatternsApiEndpoints =
            List.of("**/auth/token",
            		"**/usermanagement/register",
                    "/**/swagger-ui/**",
                    "/**/swagger-resources/**",
                    "/**/v2/api-docs/**");

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public Predicate<ServerHttpRequest> isSecured =
            request -> openPatternsApiEndpoints
                    .stream()
                    .noneMatch(pattern -> antPathMatcher.match(pattern, request.getURI().getPath()));
}