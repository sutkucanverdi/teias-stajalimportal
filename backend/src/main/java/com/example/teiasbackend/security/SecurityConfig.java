package com.example.teiasbackend.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.izinli-adresler}")
    private String izinliAdresler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/aktivasyon").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/departmanlar", "/api/departmanlar/*",
                                "/api/belge-turleri", "/api/sistem/bilgi").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                jsonHata(res, 401, "Unauthorized", "Oturum açmanız gerekiyor veya oturumunuzun süresi doldu.", req.getRequestURI()))
                        .accessDeniedHandler((req, res, ex) ->
                                jsonHata(res, 403, "Forbidden", "Bu işlem için yetkiniz bulunmamaktadır.", req.getRequestURI()))
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** JwtAuthFilter yalnızca security zincirinde çalışsın, servlet filtresi olarak ikinci kez kaydedilmesin. */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFiltreKaydi(JwtAuthFilter filtre) {
        FilterRegistrationBean<JwtAuthFilter> kayit = new FilterRegistrationBean<>(filtre);
        kayit.setEnabled(false);
        return kayit;
    }

    /** Kimlik doğrulama JWT filtresinde yapılır; Spring'in varsayılan bellek içi kullanıcısını devre dışı bırakır. */
    @Bean
    public UserDetailsService userDetailsService() {
        return kullaniciAdi -> {
            throw new UsernameNotFoundException(kullaniciAdi);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(izinliAdresler.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static void jsonHata(HttpServletResponse res, int durum, String hata, String mesaj, String yol) throws java.io.IOException {
        res.setStatus(durum);
        res.setContentType("application/json");
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write("""
                {"zaman":"%s","durum":%d,"hata":"%s","mesaj":"%s","yol":"%s"}"""
                .formatted(LocalDateTime.now(), durum, hata, mesaj, yol.replace("\"", "")));
    }
}
