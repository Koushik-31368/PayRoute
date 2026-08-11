package com.payroute.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Global CORS configuration.
 *
 * Allowed origins:
 *   1. http://localhost:*  — local dev (React Vite server, Docker frontend)
 *   2. CORS_ALLOWED_ORIGIN env var — set this on Render/Railway/etc to your
 *      Vercel frontend URL (e.g. https://pay-route.vercel.app).
 *      Falls back to the default Vercel URL if the env var is not set.
 *
 * Why env var instead of hardcoding?
 *   Keeps credentials out of source code and lets you change the allowed
 *   origin without a redeploy of the backend — just update the env var.
 *
 * SockJS note:
 *   SockJS makes its initial handshake as a plain HTTP request (/ws/info),
 *   which goes through this CORS filter *before* the WebSocket upgrade.
 *   So this filter must allow the Vercel origin for WebSocket to work too,
 *   even though WebSocketConfig already sets setAllowedOriginPatterns("*").
 */
@Configuration
public class AppConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        // Always allow local dev origins
        config.addAllowedOriginPattern("http://localhost:*");

        // Production frontend — read from env var, fall back to the real Vercel URL.
        // To change the allowed origin without a redeploy, update CORS_ALLOWED_ORIGIN
        // in your backend hosting platform's environment variables (e.g. Render).
        String productionOrigin = System.getenv()
                .getOrDefault("CORS_ALLOWED_ORIGIN", "https://pay-route-gules.vercel.app");
        config.addAllowedOriginPattern(productionOrigin);

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
