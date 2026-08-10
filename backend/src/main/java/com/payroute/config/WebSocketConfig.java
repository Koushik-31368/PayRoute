package com.payroute.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP WebSocket configuration.
 *
 * Key concepts:
 *
 *  1. enableSimpleBroker("/topic")
 *     Uses Spring's built-in in-memory message broker for topics.
 *     "/topic/..." is for broadcast (one-to-many).
 *     In production, you'd replace this with a full broker (RabbitMQ/Redis)
 *     for horizontal scaling — the simple broker only works on a single node.
 *
 *  2. setApplicationDestinationPrefixes("/app")
 *     Messages sent TO "/app/..." are routed to @MessageMapping methods.
 *     We don't need client-to-server messaging right now (only server-push),
 *     but this allows future extension (e.g. a chat, or client acks).
 *
 *  3. addEndpoint("/ws").withSockJS()
 *     "/ws" is the WebSocket handshake URL.
 *     SockJS provides a transparent HTTP fallback (long-polling, XHR streaming)
 *     for environments that block WebSocket connections.
 *     setAllowedOriginPatterns("*") permits the React dev server (localhost:5173)
 *     to connect — tighten this in production.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // tighten for production
                .withSockJS();
    }
}
