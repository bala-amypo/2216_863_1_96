package com.example.demo.service;

import com.example.demo.model.TeamCapacityConfig;

public interface TeamCapacityRuleService {
    TeamCapacityConfig createRule(TeamCapacityConfig config);
    TeamCapacityConfig updateRule(Long id, TeamCapacityConfig config);
    TeamCapacityConfig getRuleByTeam(String teamName);
}