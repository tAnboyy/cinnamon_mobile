package com.cinnamon.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @GetMapping("/orders/live")
    public ResponseEntity<String> liveOrders() {
        return ResponseEntity.ok("live orders stub");
    }

    @PostMapping("/orders/status")
    public ResponseEntity<String> updateStatus() {
        return ResponseEntity.ok("update status stub");
    }
}
