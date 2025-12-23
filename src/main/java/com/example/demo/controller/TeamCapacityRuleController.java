package com.example.demo.controller;

import com.example.demo.model.TeamCapacityConfig;
import com.example.demo.service.TeamCapacityRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/capacity-rules")
public class TeamCapacityRuleController {
    
    private final TeamCapacityRuleService capacityRuleService;
    
    public TeamCapacityRuleController(TeamCapacityRuleService capacityRuleService) {
        this.capacityRuleService = capacityRuleService;
    }
    
    @PostMapping
    public ResponseEntity<TeamCapacityConfig> create(@RequestBody TeamCapacityConfig config) {
        TeamCapacityConfig created = capacityRuleService.createRule(config);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TeamCapacityConfig> update(@PathVariable Long id, @RequestBody TeamCapacityConfig config) {
        TeamCapacityConfig updated = capacityRuleService.updateRule(id, config);
        return ResponseEntity.ok(updated);
    }
    
    @GetMapping("/team/{teamName}")
    public ResponseEntity<TeamCapacityConfig> getByTeam(@PathVariable String teamName) {
        TeamCapacityConfig config = capacityRuleService.getRuleByTeam(teamName);
        return ResponseEntity.ok(config);
    }
}