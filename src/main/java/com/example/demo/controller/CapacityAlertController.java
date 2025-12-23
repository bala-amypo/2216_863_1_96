package com.example.demo.controller;

import com.example.demo.model.CapacityAlert;
import com.example.demo.repository.CapacityAlertRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/capacity-alerts")
public class CapacityAlertController {
    
    private final CapacityAlertRepository alertRepository;
    
    public CapacityAlertController(CapacityAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }
    
    @PostMapping
    public ResponseEntity<CapacityAlert> create(@RequestBody CapacityAlert alert) {
        CapacityAlert saved = alertRepository.save(alert);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CapacityAlert> getById(@PathVariable Long id) {
        return alertRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/team/{teamName}")
    public ResponseEntity<List<CapacityAlert>> getAlerts(@PathVariable String teamName,
                                                        @RequestParam LocalDate start,
                                                        @RequestParam LocalDate end) {
        List<CapacityAlert> alerts = alertRepository.findByTeamNameAndDateBetween(teamName, start, end);
        return ResponseEntity.ok(alerts);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        alertRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}