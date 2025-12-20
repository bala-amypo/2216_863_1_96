package com.example.demo.service.impl;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.TeamCapacityConfig;
import com.example.demo.repository.TeamCapacityConfigRepository;
import com.example.demo.service.TeamCapacityRuleService;
import org.springframework.stereotype.Service;

@Service
public class TeamCapacityRuleServiceImpl implements TeamCapacityRuleService {
    private final TeamCapacityConfigRepository teamCapacityConfigRepository;

    public TeamCapacityRuleServiceImpl(TeamCapacityConfigRepository teamCapacityConfigRepository) {
        this.teamCapacityConfigRepository = teamCapacityConfigRepository;
    }

    @Override
    public TeamCapacityConfig createRule(TeamCapacityConfig rule) {
        validateRule(rule);
        return teamCapacityConfigRepository.save(rule);
    }

    @Override
    public TeamCapacityConfig updateRule(Long id, TeamCapacityConfig updatedRule) {
        TeamCapacityConfig existing = teamCapacityConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team capacity rule not found"));
        
        validateRule(updatedRule);
        
        existing.setTeamName(updatedRule.getTeamName());
        existing.setTotalHeadcount(updatedRule.getTotalHeadcount());
        existing.setMinCapacityPercent(updatedRule.getMinCapacityPercent());
        
        return teamCapacityConfigRepository.save(existing);
    }

    @Override
    public TeamCapacityConfig getRuleByTeam(String teamName) {
        return teamCapacityConfigRepository.findByTeamName(teamName)
                .orElseThrow(() -> new ResourceNotFoundException("Capacity config not found"));
    }

    private void validateRule(TeamCapacityConfig rule) {
        if (rule.getTotalHeadcount() <= 0) {
            throw new BadRequestException("Total headcount must be greater than 0");
        }
        if (rule.getMinCapacityPercent() < 1 || rule.getMinCapacityPercent() > 100) {
            throw new BadRequestException("Min capacity percent must be between 1 and 100");
        }
    }
}