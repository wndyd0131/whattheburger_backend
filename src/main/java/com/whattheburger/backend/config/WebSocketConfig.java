package com.whattheburger.backend.config;

import com.whattheburger.backend.util.OrderTrackingWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    @Value("${client.url}")
    private String clientUrl;

    private final OrderTrackingWebSocketHandler orderTrackingWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderTrackingWebSocketHandler, "/ws/track") // register handler to URL
                .setAllowedOrigins(clientUrl);
//                .withSockJS(); // activate fallback, providing alternative for browser not supporting websocket
    }
}
