package com.orderengine.dto;

public class FailureSimulationConfig {
    private boolean enabled;
    private double failureRatePercentage;
    private boolean transientFailuresOnly;
    private boolean forcePermanentFailure;
    private int simulatedProcessingDelayMs;

    public FailureSimulationConfig() {}

    public FailureSimulationConfig(boolean enabled, double failureRatePercentage, boolean transientFailuresOnly, boolean forcePermanentFailure, int simulatedProcessingDelayMs) {
        this.enabled = enabled;
        this.failureRatePercentage = failureRatePercentage;
        this.transientFailuresOnly = transientFailuresOnly;
        this.forcePermanentFailure = forcePermanentFailure;
        this.simulatedProcessingDelayMs = simulatedProcessingDelayMs;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public double getFailureRatePercentage() { return failureRatePercentage; }
    public void setFailureRatePercentage(double failureRatePercentage) { this.failureRatePercentage = failureRatePercentage; }

    public boolean isTransientFailuresOnly() { return transientFailuresOnly; }
    public void setTransientFailuresOnly(boolean transientFailuresOnly) { this.transientFailuresOnly = transientFailuresOnly; }

    public boolean isForcePermanentFailure() { return forcePermanentFailure; }
    public void setForcePermanentFailure(boolean forcePermanentFailure) { this.forcePermanentFailure = forcePermanentFailure; }

    public int getSimulatedProcessingDelayMs() { return simulatedProcessingDelayMs; }
    public void setSimulatedProcessingDelayMs(int simulatedProcessingDelayMs) { this.simulatedProcessingDelayMs = simulatedProcessingDelayMs; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean enabled;
        private double failureRatePercentage;
        private boolean transientFailuresOnly;
        private boolean forcePermanentFailure;
        private int simulatedProcessingDelayMs;

        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder failureRatePercentage(double failureRatePercentage) { this.failureRatePercentage = failureRatePercentage; return this; }
        public Builder transientFailuresOnly(boolean transientFailuresOnly) { this.transientFailuresOnly = transientFailuresOnly; return this; }
        public Builder forcePermanentFailure(boolean forcePermanentFailure) { this.forcePermanentFailure = forcePermanentFailure; return this; }
        public Builder simulatedProcessingDelayMs(int simulatedProcessingDelayMs) { this.simulatedProcessingDelayMs = simulatedProcessingDelayMs; return this; }

        public FailureSimulationConfig build() {
            return new FailureSimulationConfig(enabled, failureRatePercentage, transientFailuresOnly, forcePermanentFailure, simulatedProcessingDelayMs);
        }
    }
}
