package com.example.demo.service.impl;

import com.example.demo.dto.LeaveRequestDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.EmployeeProfile;
import com.example.demo.model.LeaveRequest;
import com.example.demo.repository.EmployeeProfileRepository;
import com.example.demo.repository.LeaveRequestRepository;
import com.example.demo.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRepository;
    private final EmployeeProfileRepository employeeRepository;

    @Autowired
    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRepository, EmployeeProfileRepository employeeRepository) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public LeaveRequestDto create(LeaveRequestDto dto) {
        EmployeeProfile employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date must be before or equal to end date");
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setType(dto.getType());
        leave.setReason(dto.getReason());
        leave.setStatus("PENDING");

        LeaveRequest saved = leaveRepository.save(leave);
        return convertToDto(saved);
    }

    @Override
    public LeaveRequestDto approve(Long id) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        leave.setStatus("APPROVED");
        LeaveRequest saved = leaveRepository.save(leave);
        return convertToDto(saved);
    }

    @Override
    public LeaveRequestDto reject(Long id) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        leave.setStatus("REJECTED");
        LeaveRequest saved = leaveRepository.save(leave);
        return convertToDto(saved);
    }

    @Override
    public List<LeaveRequestDto> getByEmployee(Long employeeId) {
        EmployeeProfile employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return leaveRepository.findByEmployee(employee)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDto> getOverlappingForTeam(String teamName, LocalDate start, LocalDate end) {
        return leaveRepository.findApprovedOverlappingForTeam(teamName, start, end)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private LeaveRequestDto convertToDto(LeaveRequest leave) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployee().getId());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setType(leave.getType());
        dto.setStatus(leave.getStatus());
        dto.setReason(leave.getReason());
        return dto;
    }
}