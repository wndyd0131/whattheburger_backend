package com.whattheburger.backend.config.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@Component
@Slf4j
public class OrderTrackingWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Websocket Session Established");

        final int minTime = 5; // 1 minute
        final int maxTime = 15; // 3 minutes
        final int randomDelay = ThreadLocalRandom.current().nextInt(minTime, maxTime);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(() -> {
            Map<String, Integer> delayMessage = Map.of(
                    "delay", randomDelay
            );
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(delayMessage)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, TimeUnit.SECONDS);
        scheduledExecutorService.schedule(() -> {
            Map<String, Object> readyFlag = Map.of(
                    "type", "PREP_ALERT",
                    "payload", Map.of(
                            "status", "PREP_COMPLETE"
                    )
            );
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(readyFlag)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, randomDelay, TimeUnit.SECONDS);
        scheduledExecutorService.shutdown();
        sessions.put(session.getId(), session);
        log.info("session size {}", sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        session.sendMessage(new TextMessage("Server sent: " + payload));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Websocket Session Closed");
    }

    public void sendUpdate(String orderId, Object data) {
        WebSocketSession session = sessions.get(orderId);
        if (session != null) {
            try {
                String jsonData = objectMapper.writeValueAsString(data);
                session.sendMessage(new TextMessage(jsonData));
                log.info("Sent to order {}", orderId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
