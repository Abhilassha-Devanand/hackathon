package com.orderengine.service;

import com.orderengine.dto.FailureSimulationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class FailureSimulatorService {

    private static final Logger log = LoggerFactory.getLogger(FailureSimulatorService.class);

    private final FailureSimulationConfig config = FailureSimulationConfig.builder()
            .enabled(false)
            .failureRatePercentage(0.0)
            .transientFailuresOnly(true)
            .forcePermanentFailure(false)
            .simulatedProcessingDelayMs(0)
            .build();

    private final Random random = new Random();

    public FailureSimulationConfig getConfig() {
        return config;
    }

    public void updateConfig(FailureSimulationConfig newConfig) {
        config.setEnabled(newConfig.isEnabled());
        config.setFailureRatePercentage(newConfig.getFailureRatePercentage());
        config.setTransientFailuresOnly(newConfig.isTransientFailuresOnly());
        config.setForcePermanentFailure(newConfig.isForcePermanentFailure());
        config.setSimulatedProcessingDelayMs(newConfig.getSimulatedProcessingDelayMs());
        log.info("Updated Failure Simulation Config: enabled={}, rate={}%", config.isEnabled(), config.getFailureRatePercentage());
    }

    public void maybeSimulateFailure(Long orderId) {
        if (!config.isEnabled()) {
            return;
        }

        if (config.getSimulatedProcessingDelayMs() > 0) {
            try {
                Thread.sleep(config.getSimulatedProcessingDelayMs());
            } catch (InterruptedException ignored) {}
        }

        double rolled = random.nextDouble() * 100.0;
        if (rolled < config.getFailureRatePercentage()) {
            if (config.isForcePermanentFailure()) {
                log.warn("FailureSimulator: Triggering PERMANENT failure for Order #{}", orderId);
                throw new IllegalArgumentException("Simulated Permanent Error (Invalid Payment Account)");
            } else {
                log.warn("FailureSimulator: Triggering TRANSIENT failure for Order #{}", orderId);
                throw new RuntimeException("Simulated Transient Network/Database Connection Timeout");
            }
        }
    }
}
