package com.cinnamon.backend.controller;

import com.cinnamon.backend.model.OrderRequest;
import com.cinnamon.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(value = "/place", consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> place(@RequestBody OrderRequest orderRequest) throws ExecutionException, InterruptedException {
        log.info("[OrderController] place userId={} items={} total={}", orderRequest.userId(), orderRequest.items() != null ? orderRequest.items().size() : 0, orderRequest.totalAmount());
        String orderId = orderService.placeOrder(orderRequest);
        log.info("[OrderController] place OK id={}", orderId);
        return ResponseEntity.ok("Order placed with ID: " + orderId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> userOrders(@PathVariable String userId) throws ExecutionException, InterruptedException {
        log.info("[OrderController] userOrders userId={}", userId);
        var orders = orderService.getUserOrders(userId);
        log.info("[OrderController] userOrders OK count={}", orders.size());
        return ResponseEntity.ok(orders);
    }
}
