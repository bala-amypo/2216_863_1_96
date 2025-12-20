package com.example.demo.controller;

import com.example.demo.dto.EmployeeProfileDto;
import com.example.demo.service.EmployeeProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeProfileController {
    private final EmployeeProfileService employeeProfileService;

    public EmployeeProfileController(EmployeeProfileService employeeProfileService) {
        this.employeeProfileService = employeeProfileService;
    }

    @PostMapping
    public ResponseEntity<EmployeeProfileDto> create(@RequestBody EmployeeProfileDto dto) {
        return ResponseEntity.ok(employeeProfileService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeProfileDto> update(@PathVariable Long id, @RequestBody EmployeeProfileDto dto) {
        return ResponseEntity.ok(employeeProfileService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeProfileDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeProfileService.getById(id));
    }

    @GetMapping("/team/{teamName}")
    public ResponseEntity<List<EmployeeProfileDto>> getByTeam(@PathVariable String teamName) {
        return ResponseEntity.ok(employeeProfileService.getByTeam(teamName));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeProfileDto>> getAll() {
        return ResponseEntity.ok(employeeProfileService.getAll());
    }
}