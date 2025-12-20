package com.example.demo.service.impl;

import com.example.demo.dto.CapacityAnalysisResultDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.CapacityAlert;
import com.example.demo.model.LeaveRequest;
import com.example.demo.model.TeamCapacityConfig;
import com.example.demo.repository.CapacityAlertRepository;
import com.example.demo.repository.EmployeeProfileRepository;
import com.example.demo.repository.LeaveRequestRepository;
import com.example.demo.repository.TeamCapacityConfigRepository;
import com.example.demo.service.CapacityAnalysisService;
import com.example.demo.util.DateRangeUtil;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CapacityAnalysisServiceImpl implements CapacityAnalysisService {
    private final TeamCapacityConfigRepository teamCapacityConfigRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final CapacityAlertRepository capacityAlertRepository;

    public CapacityAnalysisServiceImpl(TeamCapacityConfigRepository teamCapacityConfigRepository,
                                      EmployeeProfileRepository employeeProfileRepository,
                                      LeaveRequestRepository leaveRequestRepository,
                                      CapacityAlertRepository capacityAlertRepository) {
        this.teamCapacityConfigRepository = teamCapacityConfigRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.capacityAlertRepository = capacityAlertRepository;
    }

    @Override
    public CapacityAnalysisResultDto analyzeTeamCapacity(String teamName, LocalDate start, LocalDate end) {
        if (start.isAfter(end) || start.isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date or future");
        }

        TeamCapacityConfig config = teamCapacityConfigRepository.findByTeamName(teamName)
                .orElseThrow(() -> new ResourceNotFoundException("Capacity config not found"));

        if (config.getTotalHeadcount() <= 0) {
            throw new BadRequestException("Invalid total headcount");
        }

        List<LocalDate> dateRange = DateRangeUtil.daysBetween(start, end);
        List<LeaveRequest> overlappingLeaves = leaveRequestRepository.findApprovedOverlappingForTeam(teamName, start, end);
        
        Map<LocalDate, Double> capacityByDate = new HashMap<>();
        boolean risky = false;

        for (LocalDate date : dateRange) {
            long leavesOnDate = overlappingLeaves.stream()
                    .filter(leave -> !date.isBefore(leave.getStartDate()) && !date.isAfter(leave.getEndDate()))
                    .count();
            
            double availableEmployees = config.getTotalHeadcount() - leavesOnDate;
            double capacityPercent = (availableEmployees / config.getTotalHeadcount()) * 100;
            
            capacityByDate.put(date, capacityPercent);
            
            if (capacityPercent < config.getMinCapacityPercent()) {
                risky = true;
                CapacityAlert alert = new CapacityAlert(teamName, date, "HIGH", 
                        "Capacity below minimum threshold: " + capacityPercent + "%");
                capacityAlertRepository.save(alert);
            }
        }

        return new CapacityAnalysisResultDto(risky, capacityByDate);
    }
}