package com.orderengine.controller;

import com.orderengine.concurrency.OrderThreadPoolManager;
import com.orderengine.dto.FailureSimulationConfig;
import com.orderengine.dto.SystemMetricsDTO;
import com.orderengine.dto.ThreadPoolMetricsDTO;
import com.orderengine.service.FailureSimulatorService;
import com.orderengine.service.MetricsService;
import com.orderengine.websocket.EventBroadcaster;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class MetricsController {

    private final MetricsService metricsService;
    private final OrderThreadPoolManager threadPoolManager;
    private final FailureSimulatorService failureSimulatorService;
    private final EventBroadcaster eventBroadcaster;

    public MetricsController(MetricsService metricsService,
                             OrderThreadPoolManager threadPoolManager,
                             FailureSimulatorService failureSimulatorService,
                             EventBroadcaster eventBroadcaster) {
        this.metricsService = metricsService;
        this.threadPoolManager = threadPoolManager;
        this.failureSimulatorService = failureSimulatorService;
        this.eventBroadcaster = eventBroadcaster;
    }

    @GetMapping("/metrics")
    public ResponseEntity<SystemMetricsDTO> getSystemMetrics() {
        return ResponseEntity.ok(metricsService.getSystemMetrics());
    }

    @GetMapping("/threadpool")
    public ResponseEntity<ThreadPoolMetricsDTO> getThreadPoolMetrics() {
        return ResponseEntity.ok(threadPoolManager.getMetrics());
    }

    @PostMapping("/threadpool/size")
    public ResponseEntity<ThreadPoolMetricsDTO> updateThreadPoolSize(@RequestParam int size) {
        threadPoolManager.setPoolSize(size);
        return ResponseEntity.ok(threadPoolManager.getMetrics());
    }

    @GetMapping("/simulation")
    public ResponseEntity<FailureSimulationConfig> getSimulationConfig() {
        return ResponseEntity.ok(failureSimulatorService.getConfig());
    }

    @PostMapping("/simulation")
    public ResponseEntity<FailureSimulationConfig> updateSimulationConfig(@RequestBody FailureSimulationConfig config) {
        failureSimulatorService.updateConfig(config);
        return ResponseEntity.ok(failureSimulatorService.getConfig());
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter streamEvents() {
        return eventBroadcaster.registerClient();
    }
}
