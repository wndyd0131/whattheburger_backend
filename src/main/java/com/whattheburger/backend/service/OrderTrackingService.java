package com.whattheburger.backend.service;

import com.whattheburger.backend.util.OrderTrackingWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderTrackingService {

    private final OrderTrackingWebSocketHandler orderTrackingWebSocketHandler;
    public void sendReadyFlag(String orderNumber) {
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            Map<String, Object> readyFlag = Map.of(
                    "type", "PREP_ALERT",
                    "payload", Map.of(
                            "status", "COMPLETE"
                    )
            );
        }, 10, TimeUnit.SECONDS);
    }
}
