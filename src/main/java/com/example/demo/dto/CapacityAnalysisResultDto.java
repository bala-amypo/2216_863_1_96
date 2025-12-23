package com.example.demo.dto;

import java.time.LocalDate;
import java.util.Map;

public class CapacityAnalysisResultDto {
    private boolean risky;
    private Map<LocalDate, Double> capacityByDate;

    public CapacityAnalysisResultDto() {}

    public boolean isRisky() { return risky; }
    public void setRisky(boolean risky) { this.risky = risky; }

    public Map<LocalDate, Double> getCapacityByDate() { return capacityByDate; }
    public void setCapacityByDate(Map<LocalDate, Double> capacityByDate) { this.capacityByDate = capacityByDate; }
}