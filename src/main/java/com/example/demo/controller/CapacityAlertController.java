package com.example.demo.controller;

import com.example.demo.dto.CapacityAnalysisResultDto;
import com.example.demo.model.CapacityAlert;
import com.example.demo.repository.CapacityAlertRepository;
import com.example.demo.service.CapacityAnalysisService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/capacity-alerts")
public class CapacityAlertController {
    private final CapacityAnalysisService capacityAnalysisService;
    private final CapacityAlertRepository capacityAlertRepository;

    public CapacityAlertController(CapacityAnalysisService capacityAnalysisService, 
                                  CapacityAlertRepository capacityAlertRepository) {
        this.capacityAnalysisService = capacityAnalysisService;
        this.capacityAlertRepository = capacityAlertRepository;
    }

    @PostMapping("/analyze")
    public ResponseEntity<CapacityAnalysisResultDto> analyze(
            @RequestParam String teamName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(capacityAnalysisService.analyzeTeamCapacity(teamName, start, end));
    }

    @GetMapping("/team/{teamName}")
    public ResponseEntity<List<CapacityAlert>> getAlerts(
            @PathVariable String teamName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(capacityAlertRepository.findByTeamNameAndDateBetween(teamName, start, end));
    }
}