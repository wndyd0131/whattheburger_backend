package com.whattheburger.backend.util;

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
import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.order.OrderSession;
import com.whattheburger.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderTrackingWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderService orderService;

    @Value("${google.apiKey}")
    private String secretKey;
    private final GeoApiContext geoApiContext;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("session id: {}", session.getId());
        log.info("Websocket Session Established");

        String query = session.getUri().getQuery();
        Map<String, String> queryMap = parseQueryString(query);

        String orderSessionId = Optional.ofNullable(queryMap.get("order_session_id"))
                .orElseThrow(() -> new IllegalArgumentException());
        OrderSession orderSession = orderService.loadOrderSessionByOrderSessionId(UUID.fromString(orderSessionId));
        log.info("order session id: {}", orderSessionId);

        sessionMap.put(orderSessionId, session);


        // create new session
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Paid
        // Confirm Order
        // Preparation
        // Delivery

        scheduleStep(scheduledExecutorService, orderSession);

        log.info("session size {}", sessionMap.size());
    }

    private void scheduleStep(ScheduledExecutorService scheduledExecutorService, OrderSession orderSession) {
        // check orderStatus, if then see the time, if time elapsed then go to next step
            // if time passed, then go to next step (Cooking)
            // if not, then send update to the client

        WebSocketSession session = sessionMap.get(orderSession.getSessionId().toString());
        log.info("order session id: {}", orderSession.getSessionId());
        log.info("session: {}", session);

        // current - (createdTime + (lastModifiedTime - createdTime) + duration)
        //

        // websocket connection
        // orderSessionId -> orderSession -> status, modifiedTime, duration

        while (true) {
            OrderStatus orderStatus = orderSession.getOrderStatus();
            log.info("Current order status: {}", orderStatus.name());

            if (orderStatus == OrderStatus.COMPLETED) {
                Map<String, Object> statusObject = toStatusData(orderStatus);
                sendUpdate(session, statusObject);
                cleanUp(session, scheduledExecutorService, orderSession);
                return;
            }
            Long orderStatusModifiedTime = orderSession.getOrderStatusModifiedTime();
            Integer orderStatusDuration = orderSession.getOrderStatusDuration();
            Long orderStatusEndTime = orderStatusModifiedTime + orderStatusDuration;
            Long remaining = orderStatusEndTime - System.currentTimeMillis();

            if (remaining <= 0) { // if elapsed, then transition to next step
                OrderStatus nextStatus = getNextStatus(orderStatus);
                if (nextStatus == OrderStatus.COMPLETED) {
                    Map<String, Object> statusObject = toStatusData(nextStatus);
                    orderService.updateOrderSessionOrderStatus(orderSession, nextStatus, orderStatusEndTime, 0); // CONFIRMED
                    sendUpdate(session, statusObject);
                    cleanUp(session, scheduledExecutorService, orderSession);
                    return;
                }
                Integer newDuration = getRandomDuration(nextStatus);
                orderService.updateOrderSessionOrderStatus(orderSession, nextStatus, orderStatusEndTime, newDuration); // CONFIRMED

                continue;
            } // if not, then re-execute after remaining time
            log.info("Current order status: {}", orderStatus.name());

            sendUpdate(session, toStatusData(orderStatus));
            scheduledExecutorService.schedule(() -> {
                OrderSession updatedOrderSession = orderService.loadOrderSessionByOrderSessionId(orderSession.getSessionId()); // getting up-to-date orderSession from redis
                scheduleStep(scheduledExecutorService, updatedOrderSession);
            }, remaining, TimeUnit.MILLISECONDS);
            return;
        }
    }

    private OrderStatus getNextStatus(OrderStatus orderStatus) {
        switch (orderStatus) {
            case PENDING -> {
                return OrderStatus.CONFIRMED;
            }
            case CONFIRMED -> {
                return OrderStatus.PREPARING;
            }
            case PREPARING -> {
                return OrderStatus.DELIVERING;
            }
            case DELIVERING -> {
                return OrderStatus.COMPLETED;
            }
            default -> {
                return null;
            }
        }
    }

    private Integer getRandomDuration(OrderStatus orderStatus) {
        switch (orderStatus) {
            case CONFIRMED -> {
                return 0;
            }
            case PREPARING -> {
                return ThreadLocalRandom.current().nextInt(40, 60) * 1000;
            }
            case DELIVERING -> {
                return ThreadLocalRandom.current().nextInt(60, 180) * 1000;
            }
            default -> {
                return null;
            }
        }
    }

    private Map<String, String> parseQueryString(String query) {
        Map<String, String> queryMap = new HashMap<>();
        if (query == null) return queryMap;

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2)
                queryMap.put(pair[0], pair[1]);
        }
        return queryMap;
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

    private Map<String, Object> toStatusData(OrderStatus orderStatus) {
        return Map.of("payload", Map.of("orderStatus", orderStatus.name()));
    }

    public void sendUpdate(WebSocketSession session, Object data) {
        log.info("send update to session id: {}", session.getId());
        if (session != null) {
            try {
                String jsonData = objectMapper.writeValueAsString(data);
                session.sendMessage(new TextMessage(jsonData));
                log.info("Sent {}", jsonData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cleanUp(WebSocketSession session, ScheduledExecutorService scheduledExecutorService, OrderSession orderSession) {
        UUID orderSessionId = orderSession.getSessionId();
        sessionMap.remove(orderSessionId.toString());
        scheduledExecutorService.shutdown();
        try {
            if (session != null && session.isOpen()) session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
