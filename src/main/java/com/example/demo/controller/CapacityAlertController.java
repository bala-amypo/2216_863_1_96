package com.example.demo.controller;

import com.example.demo.dto.CapacityAnalysisResultDto;
import com.example.demo.model.CapacityAlert;
import com.example.demo.repository.CapacityAlertRepository;
import com.example.demo.service.CapacityAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/capacity-alerts")
@Tag(name = "Capacity Analysis", description = "Team capacity analysis and alerts APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class CapacityAlertController {

    private final CapacityAnalysisService capacityAnalysisService;
    private final CapacityAlertRepository alertRepository;

    @Autowired
    public CapacityAlertController(CapacityAnalysisService capacityAnalysisService, CapacityAlertRepository alertRepository) {
        this.capacityAnalysisService = capacityAnalysisService;
        this.alertRepository = alertRepository;
    }

    @PostMapping("/analyze")
    @Operation(summary = "Analyze team capacity", description = "Analyze team capacity for a date range and generate alerts if needed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analysis completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid analysis parameters"),
        @ApiResponse(responseCode = "404", description = "Team configuration not found")
    })
    public ResponseEntity<CapacityAnalysisResultDto> analyzeCapacity(
            @RequestParam String teamName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(capacityAnalysisService.analyzeTeamCapacity(teamName, start, end));
    }

    @GetMapping("/team/{teamName}")
    @Operation(summary = "Get team alerts", description = "Get capacity alerts for a team within a date range")
    public ResponseEntity<List<CapacityAlert>> getAlertsByTeam(
            @PathVariable String teamName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(alertRepository.findByTeamNameAndDateBetween(teamName, start, end));
    }
}