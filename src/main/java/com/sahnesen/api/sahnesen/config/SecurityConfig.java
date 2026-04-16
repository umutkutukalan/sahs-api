package com.sahnesen.api.sahnesen.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sahnesen.api.sahnesen.security.JwtAuthenticationFilter;
import com.sahnesen.api.sahnesen.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // 🔓 AUTH & USER
                .requestMatchers(
                        "/users/login",
                        "/users/register",
                        "/users/me",          // 👈 KRİTİK
                        "/auth/**",
                        "/oauth2/**"
                ).permitAll()

                // 🔓 PUBLIC READ
                .requestMatchers(
                        HttpMethod.GET,
                        "/projects/**",
                        "/blogs/**",
                        "/musics/**"
                ).permitAll()

                // 🔓 PREFLIGHT (CORS)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 🔒 WRITE OPERATIONS
                .requestMatchers(
                        HttpMethod.POST,
                        "/projects/**",
                        "/blogs/**",
                        "/musics/**"
                ).authenticated()

                .requestMatchers(
                        HttpMethod.PUT,
                        "/projects/**",
                        "/blogs/**",
                        "/musics/**"
                ).authenticated()

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/projects/**",
                        "/blogs/**",
                        "/musics/**"
                ).authenticated()

                .anyRequest().permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/auth/logout")
                    .logoutSuccessHandler((req, res, auth) ->
                            res.setStatus(HttpServletResponse.SC_OK)
                    )
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID", "authToken")
            );

        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    // 🌍 CORS CONFIG (COOKIE-FRIENDLY)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173", "http://localhost:3000"));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"));

        config.setAllowedHeaders(List.of("*"));

        config.setExposedHeaders(List.of(
                "Authorization",
                "Set-Cookie" // 👈 önemli
        ));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}