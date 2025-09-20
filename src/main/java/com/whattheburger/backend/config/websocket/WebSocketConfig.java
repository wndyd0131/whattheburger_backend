package com.whattheburger.backend.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final OrderTrackingWebSocketHandler orderTrackingWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderTrackingWebSocketHandler, "/ws/track") // register handler to URL
                .setAllowedOriginPatterns("*");
//                .withSockJS(); // activate fallback, providing alternative for browser not supporting websocket
    }
}
