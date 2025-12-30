package com.whattheburger.backend.util;

import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.events.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebsocketListener {

    private final OrderTrackingWebSocketHandler orderTrackingWebSocketHandler;

    @EventListener
    public void handle(OrderStatusChangedEvent event) {
        log.info("Received OrderStatusChangedEvent {}", event);
        orderTrackingWebSocketHandler.sendUpdate(
                event.orderSessionId().toString(),
                toStatusData(event.orderStatus())
        );
    }

    private Map<String, Object> toStatusData(OrderStatus orderStatus) {
        return Map.of("payload", Map.of("orderStatus", orderStatus.name()));
    }
}
