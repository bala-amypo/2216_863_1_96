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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CapacityAnalysisServiceImpl implements CapacityAnalysisService {

    private final TeamCapacityConfigRepository capacityRepository;
    private final EmployeeProfileRepository employeeRepository;
    private final LeaveRequestRepository leaveRepository;
    private final CapacityAlertRepository alertRepository;

    @Autowired
    public CapacityAnalysisServiceImpl(TeamCapacityConfigRepository capacityRepository,
                                     EmployeeProfileRepository employeeRepository,
                                     LeaveRequestRepository leaveRepository,
                                     CapacityAlertRepository alertRepository) {
        this.capacityRepository = capacityRepository;
        this.employeeRepository = employeeRepository;
        this.leaveRepository = leaveRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    public CapacityAnalysisResultDto analyzeTeamCapacity(String teamName, LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Start date must be before or equal to end date");
        }

        TeamCapacityConfig config = capacityRepository.findByTeamName(teamName)
                .orElseThrow(() -> new ResourceNotFoundException("Capacity config not found for team: " + teamName));

        if (config.getTotalHeadcount() <= 0) {
            throw new BadRequestException("Invalid total headcount for team: " + teamName);
        }

        List<LocalDate> dateRange = DateRangeUtil.daysBetween(start, end);
        Map<LocalDate, Double> capacityByDate = new HashMap<>();
        boolean risky = false;

        for (LocalDate date : dateRange) {
            List<LeaveRequest> leavesOnDate = leaveRepository.findApprovedOverlappingForTeam(teamName, date, date);
            int employeesOnLeave = leavesOnDate.size();
            int availableEmployees = config.getTotalHeadcount() - employeesOnLeave;
            double capacityPercent = (availableEmployees * 100.0) / config.getTotalHeadcount();
            
            capacityByDate.put(date, capacityPercent);

            if (capacityPercent < config.getMinCapacityPercent()) {
                risky = true;
                CapacityAlert alert = new CapacityAlert(teamName, date, "HIGH", 
                    "Team capacity below threshold: " + capacityPercent + "%");
                alertRepository.save(alert);
            }
        }

        CapacityAnalysisResultDto result = new CapacityAnalysisResultDto();
        result.setRisky(risky);
        result.setCapacityByDate(capacityByDate);

        return result;
    }
}