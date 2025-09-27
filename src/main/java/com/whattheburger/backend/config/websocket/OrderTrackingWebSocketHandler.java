package com.whattheburger.backend.config.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.InvalidRequestException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class OrderTrackingWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${google.apiKey}")
    private String secretKey;
    private final GeoApiContext geoApiContext;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Websocket Session Established");
        log.info("secretkey {}", secretKey);

        final int minTime = 10; // 1 minute
        final int maxTime = 15; // 3 minutes
        final int randomDelay = ThreadLocalRandom.current().nextInt(minTime, maxTime);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                log.info("It starts here");
                GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, "716 Settlement St.").await();
                log.info("It stops here");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String s = gson.toJson(results[0].geometry.location);
                log.info("location {}", s);
                geoApiContext.shutdown();
            } catch (InvalidRequestException e) {
                e.printStackTrace();
                log.info("invalid request exception");
            } catch (ApiException e) {
                e.printStackTrace();
                log.info("api exception");
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.info("interrupted exception");
            } catch (IOException e) {
                e.printStackTrace();
                log.info("io exception");
            }
        });
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
