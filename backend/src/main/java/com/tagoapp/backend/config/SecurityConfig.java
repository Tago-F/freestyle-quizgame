package com.tagoapp.backend.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // (1) Spring Security に CORS 設定を適用する
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // (2) CSRF 保護を無効にする (JSON API の場合は一般的)
                .csrf(csrf -> csrf.disable())

                // (3) /api/** へのリクエストをすべて許可する
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/**").permitAll() // /api/ 以下のすべてのリクエストを許可
                        .anyRequest().authenticated() // それ以外のリクエストは認証が必要 (例: Actuator など)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // (4) 許可するオリジン (WebConfig.java と同じ設定)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://127.0.0.1:4200"));

        // (5) 許可する HTTP メソッド
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // (6) 許可するヘッダー
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // (7) 資格情報 (Cookie など) を許可する
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration); // /api/** パスにこの設定を適用

        return source;
    }
}
