package com.sudhanshu.loanmanagement.security;

import com.sudhanshu.loanmanagement.filter.RequestLoggingFilter;
import com.sudhanshu.loanmanagement.security.ratelimit.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final RequestLoggingFilter requestLoggingFilter;

    private final RateLimitingFilter rateLimitingFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(

                                // Public Authentication APIs
                                "/api/v1/auth/**",
                                "/api/v1/users/register",

                                // Swagger UI
                                "/swagger-ui/**",
                                "/swagger-ui.html",

                                // OpenAPI Documentation
                                "/api-docs/**",
                                "/api-docs",
                                "/v3/api-docs/**",
                                "/v3/api-docs",

                                // Swagger Resources
                                "/swagger-resources/**",
                                "/webjars/**",

                                // Spring Boot Actuator
                                "/actuator/health",
                                "/actuator/info"

                        ).permitAll()

                        .requestMatchers(

                                "/actuator/**"

                        ).hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                .addFilterAfter(
                        requestLoggingFilter,
                        SecurityContextHolderFilter.class
                )

                .addFilterBefore(
                        rateLimitingFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, authException) -> {

            response.setStatus(401);
            response.setContentType("application/json");

            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "Authentication required.",
                        "data": null
                    }
                    """);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {

        return (request, response, accessDeniedException) -> {

            response.setStatus(403);
            response.setContentType("application/json");

            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "Access denied.",
                        "data": null
                    }
                    """);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }
}




