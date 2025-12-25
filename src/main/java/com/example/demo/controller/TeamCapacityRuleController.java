package com.example.demo.controller;

import com.example.demo.model.TeamCapacityConfig;
import com.example.demo.service.TeamCapacityRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/capacity-rules")
@Tag(name = "Capacity Rules", description = "Team capacity configuration management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class TeamCapacityRuleController {

    private final TeamCapacityRuleService capacityRuleService;

    @Autowired
    public TeamCapacityRuleController(TeamCapacityRuleService capacityRuleService) {
        this.capacityRuleService = capacityRuleService;
    }

    @PostMapping
    @Operation(summary = "Create capacity rule", description = "Create a new team capacity configuration rule")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Capacity rule created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid capacity rule data")
    })
    public ResponseEntity<TeamCapacityConfig> createRule(@RequestBody TeamCapacityConfig rule) {
        return ResponseEntity.ok(capacityRuleService.createRule(rule));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update capacity rule", description = "Update an existing team capacity configuration rule")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Capacity rule updated successfully"),
        @ApiResponse(responseCode = "404", description = "Capacity rule not found")
    })
    public ResponseEntity<TeamCapacityConfig> updateRule(@PathVariable Long id, @RequestBody TeamCapacityConfig rule) {
        return ResponseEntity.ok(capacityRuleService.updateRule(id, rule));
    }

    @GetMapping("/team/{teamName}")
    @Operation(summary = "Get capacity rule by team", description = "Retrieve capacity configuration rule for a specific team")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Capacity rule found"),
        @ApiResponse(responseCode = "404", description = "Capacity rule not found for team")
    })
    public ResponseEntity<TeamCapacityConfig> getRuleByTeam(@PathVariable String teamName) {
        return ResponseEntity.ok(capacityRuleService.getRuleByTeam(teamName));
    }
}