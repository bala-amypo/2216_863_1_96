package com.example.demo.service.impl;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.TeamCapacityConfig;
import com.example.demo.repository.TeamCapacityConfigRepository;
import com.example.demo.service.TeamCapacityRuleService;
import org.springframework.stereotype.Service;

@Service
public class TeamCapacityRuleServiceImpl implements TeamCapacityRuleService {
    
    private final TeamCapacityConfigRepository capacityRepo;
    
    public TeamCapacityRuleServiceImpl(TeamCapacityConfigRepository capacityRepo) {
        this.capacityRepo = capacityRepo;
    }
    
    @Override
    public TeamCapacityConfig createRule(TeamCapacityConfig config) {
        validateConfig(config);
        return capacityRepo.save(config);
    }
    
    @Override
    public TeamCapacityConfig updateRule(Long id, TeamCapacityConfig config) {
        TeamCapacityConfig existing = capacityRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Capacity config not found"));
        
        validateConfig(config);
        
        existing.setTeamName(config.getTeamName());
        existing.setTotalHeadcount(config.getTotalHeadcount());
        existing.setMinCapacityPercent(config.getMinCapacityPercent());
        
        return capacityRepo.save(existing);
    }
    
    @Override
    public TeamCapacityConfig getRuleByTeam(String teamName) {
        return capacityRepo.findByTeamName(teamName)
                .orElseThrow(() -> new ResourceNotFoundException("Capacity config not found"));
    }
    
    private void validateConfig(TeamCapacityConfig config) {
        if (config.getTotalHeadcount() == null || config.getTotalHeadcount() < 1) {
            throw new BadRequestException("Invalid total headcount");
        }
        
        if (config.getMinCapacityPercent() == null || 
            config.getMinCapacityPercent() < 1 || 
            config.getMinCapacityPercent() > 100) {
            throw new BadRequestException("Min capacity percent must be between 1 and 100");
        }
    }
}