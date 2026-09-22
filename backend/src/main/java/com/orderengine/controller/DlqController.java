package com.orderengine.controller;

import com.orderengine.dto.DlqResponse;
import com.orderengine.service.DeadLetterQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dlq")
@CrossOrigin(origins = "*")
public class DlqController {

    private final DeadLetterQueueService dlqService;

    public DlqController(DeadLetterQueueService dlqService) {
        this.dlqService = dlqService;
    }

    @GetMapping
    public ResponseEntity<List<DlqResponse>> getDlqEntries() {
        return ResponseEntity.ok(dlqService.getAllDlqEntries());
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<Map<String, String>> retryDlqOrder(@PathVariable Long id) {
        dlqService.retryDlqOrder(id);
        return ResponseEntity.ok(Map.of("message", "DLQ Order #" + id + " re-queued for processing", "status", "SUCCESS"));
    }
}
