package com.example.demo.service;

import com.example.demo.dto.EmployeeProfileDto;
import java.util.List;

public interface EmployeeProfileService {
    EmployeeProfileDto create(EmployeeProfileDto dto);
    EmployeeProfileDto update(Long id, EmployeeProfileDto dto);
    void deactivate(Long id);
    EmployeeProfileDto getById(Long id);
    List<EmployeeProfileDto> getByTeam(String teamName);
    List<EmployeeProfileDto> getAll();
}