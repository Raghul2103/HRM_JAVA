package com.hrm.workforce.config;

import com.hrm.workforce.security.CustomAccessDeniedHandler;
import com.hrm.workforce.security.CustomAuthenticationEntryPoint;
import com.hrm.workforce.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                          CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                
                // Attendance endpoints
                .requestMatchers(HttpMethod.POST, "/api/attendance/clock-in").hasAnyRole("ADMIN", "SITE_SUPERVISOR")
                .requestMatchers(HttpMethod.POST, "/api/attendance/clock-out").hasAnyRole("ADMIN", "SITE_SUPERVISOR")
                .requestMatchers(HttpMethod.GET, "/api/attendance/active").hasAnyRole("ADMIN", "SITE_SUPERVISOR", "SITE_MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/attendance/log").hasAnyRole("ADMIN", "HR", "SITE_MANAGER")
                
                // Overtime endpoints
                .requestMatchers(HttpMethod.GET, "/api/overtime/summary/**").hasAnyRole("ADMIN", "PAYROLL_OPERATOR", "SITE_MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/overtime/settle/**").hasAnyRole("ADMIN", "PAYROLL_OPERATOR")
                
                // Management endpoints
                .requestMatchers(HttpMethod.GET, "/api/workers/**").hasAnyRole("ADMIN", "HR", "SITE_SUPERVISOR", "PAYROLL_OPERATOR", "SITE_MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/sites/**").hasAnyRole("ADMIN", "HR", "SITE_SUPERVISOR", "SITE_MANAGER")
                .requestMatchers("/api/workers/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers("/api/sites/**").hasAnyRole("ADMIN", "HR")
                
                // Default secure
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
