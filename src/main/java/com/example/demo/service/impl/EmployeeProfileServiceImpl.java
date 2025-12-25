package com.example.demo.service.impl;

import com.example.demo.dto.EmployeeProfileDto;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.EmployeeProfile;
import com.example.demo.repository.EmployeeProfileRepository;
import com.example.demo.service.EmployeeProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeProfileServiceImpl implements EmployeeProfileService {

    private final EmployeeProfileRepository employeeRepository;

    @Autowired
    public EmployeeProfileServiceImpl(EmployeeProfileRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeProfileDto create(EmployeeProfileDto dto) {
        EmployeeProfile employee = new EmployeeProfile();
        employee.setEmployeeId(dto.getEmployeeId());
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setTeamName(dto.getTeamName());
        employee.setRole(dto.getRole());
        employee.setActive(true);
        employee.setCreatedAt(LocalDateTime.now());

        EmployeeProfile saved = employeeRepository.save(employee);
        return convertToDto(saved);
    }

    @Override
    public EmployeeProfileDto update(Long id, EmployeeProfileDto dto) {
        EmployeeProfile employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (dto.getFullName() != null) employee.setFullName(dto.getFullName());
        if (dto.getTeamName() != null) employee.setTeamName(dto.getTeamName());
        if (dto.getRole() != null) employee.setRole(dto.getRole());

        EmployeeProfile saved = employeeRepository.save(employee);
        return convertToDto(saved);
    }

    @Override
    public void deactivate(Long id) {
        EmployeeProfile employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    @Override
    public EmployeeProfileDto getById(Long id) {
        EmployeeProfile employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return convertToDto(employee);
    }

    @Override
    public List<EmployeeProfileDto> getByTeam(String teamName) {
        return employeeRepository.findByTeamNameAndActiveTrue(teamName)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeProfileDto> getAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private EmployeeProfileDto convertToDto(EmployeeProfile employee) {
        EmployeeProfileDto dto = new EmployeeProfileDto();
        dto.setId(employee.getId());
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setFullName(employee.getFullName());
        dto.setEmail(employee.getEmail());
        dto.setTeamName(employee.getTeamName());
        dto.setRole(employee.getRole());
        return dto;
    }
}