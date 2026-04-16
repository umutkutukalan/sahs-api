package com.sahnesen.api.sahnesen.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sahnesen.api.sahnesen.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        System.out.println("🔍 JwtAuthenticationFilter - Request: " + request.getMethod() + " " + requestPath);

        String token = null;
        
        // 1. Önce Authorization header'ından token al (API istekleri için)
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
            System.out.println("✅ Token Authorization header'dan alındı: " + (token.length() > 20 ? token.substring(0, 20) + "..." : token));
        }
        
        // 2. Header'da token yoksa cookie'den al (browser istekleri için)
        if (token == null) {
            System.out.println("🔍 Authorization header'da token yok, cookie'leri kontrol ediliyor...");
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                System.out.println("🍪 " + cookies.length + " cookie bulundu:");
                for (Cookie cookie : cookies) {
                    String cookieValue = cookie.getValue();
                    String displayValue = cookieValue.length() > 20 ? cookieValue.substring(0, 20) + "..." : cookieValue;
                    System.out.println("   - " + cookie.getName() + " = " + displayValue);
                    
                    if ("authToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        String tokenDisplay = token.length() > 20 ? token.substring(0, 20) + "..." : token;
                        System.out.println("✅ authToken cookie'den alındı: " + tokenDisplay);
                        break;
                    }
                }
            } else {
                System.out.println("❌ Hiç cookie bulunamadı!");
            }
        }

        // 3. Token varsa authenticate et
        if (token != null) {
            try {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                System.out.println("🔐 JWT parsed - Username: " + username + ", Role: " + role);
                
                if (username != null && role != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("✅ Authentication set edildi: " + username);
                } else {
                    System.out.println("❌ Username veya role null!");
                }
            } catch (Exception e) {
                // Token geçersizse authentication yapma, sadece devam et
                System.out.println("❌ JWT Token geçersiz: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Token bulunamadı - Authentication yapılmadı");
        }

        filterChain.doFilter(request, response);
    }
}