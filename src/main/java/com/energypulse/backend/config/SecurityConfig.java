package com.energypulse.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // REST API එකක් නිසා CSRF disable
                .csrf(AbstractHttpConfigurer::disable)

                // Default HTML login page disable
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic login popup disable
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // Login API එකට authentication අවශ්‍ය නැහැ
                        .requestMatchers("/users/v1/signup","/users/v1/login","/contacts/submit").permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // අනිත් APIs වලට authentication ඕන
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}