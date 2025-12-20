package com.example.demo.service.impl;

import com.example.demo.dto.LeaveRequestDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.EmployeeProfile;
import com.example.demo.model.LeaveRequest;
import com.example.demo.repository.EmployeeProfileRepository;
import com.example.demo.repository.LeaveRequestRepository;
import com.example.demo.service.LeaveRequestService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository, 
                                   EmployeeProfileRepository employeeProfileRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeProfileRepository = employeeProfileRepository;
    }

    @Override
    public LeaveRequestDto create(LeaveRequestDto dto) {
        EmployeeProfile employee = employeeProfileRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date must be before or equal to end date");
        }
        
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setType(dto.getType());
        leaveRequest.setReason(dto.getReason());
        leaveRequest.setStatus("PENDING");
        
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToDto(saved);
    }

    @Override
    public LeaveRequestDto approve(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        
        leaveRequest.setStatus("APPROVED");
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToDto(saved);
    }

    @Override
    public LeaveRequestDto reject(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        
        leaveRequest.setStatus("REJECTED");
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToDto(saved);
    }

    @Override
    public List<LeaveRequestDto> getByEmployee(Long employeeId) {
        EmployeeProfile employee = employeeProfileRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        return leaveRequestRepository.findByEmployee(employee)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDto> getOverlappingForTeam(String teamName, LocalDate start, LocalDate end) {
        return leaveRequestRepository.findApprovedOverlappingForTeam(teamName, start, end)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private LeaveRequestDto mapToDto(LeaveRequest leaveRequest) {
        return new LeaveRequestDto(
                leaveRequest.getId(),
                leaveRequest.getEmployee().getId(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getType(),
                leaveRequest.getStatus(),
                leaveRequest.getReason()
        );
    }
}