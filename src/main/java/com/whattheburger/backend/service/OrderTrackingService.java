package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.domain.order.OrderSession;
import com.whattheburger.backend.events.OrderStatusChangedEvent;
import com.whattheburger.backend.util.OrderStatusDetail;
import com.whattheburger.backend.util.OrderTrackingWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingService {

    private final OrderTrackingWebSocketHandler orderTrackingWebSocketHandler;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

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

    public void scheduleOrder(OrderSession orderSession, Order order) {
        OrderType orderType = order.getOrderType();
        List<OrderStatus> orderStatusList = createOrderStatusList(orderType); // order process based on order type
        Map<OrderStatus, OrderStatusDetail> randomTimeframe = createRandomTimeframe(orderStatusList);// timeframe based on order process

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        for (int i = 0; i < orderStatusList.size(); i++) {
            OrderStatus orderStatus = orderStatusList.get(i);
            if (orderStatus == OrderStatus.COMPLETED)
                break;
            OrderStatus nextStatus = randomTimeframe.get(orderStatus).next();
            Integer time = randomTimeframe.get(orderStatus).time();
            log.info("order status {} has been scheduled with time {}; next status is {}", orderStatus, time, nextStatus);

            scheduledExecutorService.schedule(() -> {
                // update RDB (order status, updated time)
                // send Update through socket
                updateAndNotify(orderSession, order, nextStatus);
            },  randomTimeframe.get(orderStatus).time(), TimeUnit.SECONDS);
        }
    }

    private void updateAndNotify(OrderSession orderSession, Order order, OrderStatus orderStatus) {
        orderService.updateOrderStatus(order, orderStatus);
        log.info("Status of order {} has been updated to {}", order.getOrderNumber(), orderStatus);
        eventPublisher.publishEvent(
                new OrderStatusChangedEvent(orderSession.getSessionId(), orderStatus)
        );
    }

    private Map<OrderStatus, OrderStatusDetail> createRandomTimeframe(List<OrderStatus> orderStatusList) {
        Map<OrderStatus, OrderStatusDetail> timeframeMap = new HashMap<>();

        for (int i = 0; i < orderStatusList.size(); i++) {
            OrderStatus orderStatus = orderStatusList.get(i);
            OrderStatus previous = null;
            Integer previousTime = null;
            OrderStatus next = null;
            if (orderStatus != OrderStatus.PENDING) {
                previous = orderStatusList.get(i - 1);
                previousTime = timeframeMap.get(previous).time();
            }
            if (orderStatus != OrderStatus.COMPLETED)
                next = orderStatusList.get(i + 1);

            switch(orderStatus) {
                case PENDING -> {
                    timeframeMap.put(OrderStatus.PENDING,
                            new OrderStatusDetail(
                                    5,
                                    next
                            )
                    );
                }
                case CONFIRMING -> {
                    Integer originOffset = 5;
                    Integer boundOffset = 15;
                    timeframeMap.put(OrderStatus.CONFIRMING,
                            new OrderStatusDetail(
                                    ThreadLocalRandom.current().nextInt(previousTime + originOffset, previousTime + boundOffset),
                                    next
                            )
                    );
                }
                case PREPARING -> {
                    Integer originOffset = 30;
                    Integer boundOffset = 60;
                    timeframeMap.put(
                            OrderStatus.PREPARING,
                            new OrderStatusDetail(
                                    ThreadLocalRandom.current().nextInt(previousTime + originOffset, previousTime + boundOffset),
                                    next
                            )
                    );
                }
                case DELIVERING -> {
                    Integer originOffset = 60;
                    Integer boundOffset = 180;
                    timeframeMap.put(
                            OrderStatus.DELIVERING,
                            new OrderStatusDetail(
                                    ThreadLocalRandom.current().nextInt(previousTime + originOffset, previousTime + boundOffset),
                                    next
                            )
                    );
                }
                case COMPLETED -> {
                    timeframeMap.put(
                            OrderStatus.COMPLETED,
                            new OrderStatusDetail(
                                    previousTime + 10,
                                    null
                            )
                    );
                }
                default -> {
                    throw new IllegalStateException("Unexpected value: " + orderStatus);
                }
            }
        }
        return timeframeMap;
    }

    private List<OrderStatus> createOrderStatusList(OrderType orderType) {
        switch(orderType) {
            case DELIVERY: {
                return List.of(
                        OrderStatus.PENDING,
                        OrderStatus.CONFIRMING,
                        OrderStatus.PREPARING,
                        OrderStatus.DELIVERING,
                        OrderStatus.COMPLETED
                );
            }
            case PICK_UP: {
                return List.of(

                );
            }
            default:
                throw new IllegalStateException("Unexpected value: " + orderType);
        }
    }
}
