package com.cinnamon.backend.controller;

import com.cinnamon.backend.model.OrderRequest;
import com.cinnamon.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public ResponseEntity<String> place(@RequestBody OrderRequest orderRequest) throws ExecutionException, InterruptedException {
        String orderId = orderService.placeOrder(orderRequest);
        return ResponseEntity.ok("Order placed with ID: " + orderId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<String> userOrders(@PathVariable String userId) {
        // This would fetch orders from Firestore for the given user
        return ResponseEntity.ok("orders for " + userId);
    }
}
