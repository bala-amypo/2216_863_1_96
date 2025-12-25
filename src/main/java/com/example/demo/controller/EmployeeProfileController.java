package com.example.demo.controller;

import com.example.demo.dto.EmployeeProfileDto;
import com.example.demo.service.EmployeeProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "Employee profile management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeProfileController {

    private final EmployeeProfileService employeeService;

    @Autowired
    public EmployeeProfileController(EmployeeProfileService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @Operation(summary = "Create employee", description = "Create a new employee profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid employee data")
    })
    public ResponseEntity<EmployeeProfileDto> createEmployee(@RequestBody EmployeeProfileDto dto) {
        return ResponseEntity.ok(employeeService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Update an existing employee profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeProfileDto> updateEmployee(@PathVariable Long id, @RequestBody EmployeeProfileDto dto) {
        return ResponseEntity.ok(employeeService.update(id, dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Retrieve employee profile by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee found"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeProfileDto> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping("/team/{teamName}")
    @Operation(summary = "Get employees by team", description = "Retrieve all active employees in a team")
    public ResponseEntity<List<EmployeeProfileDto>> getEmployeesByTeam(@PathVariable String teamName) {
        return ResponseEntity.ok(employeeService.getByTeam(teamName));
    }

    @GetMapping
    @Operation(summary = "Get all employees", description = "Retrieve all employee profiles")
    public ResponseEntity<List<EmployeeProfileDto>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAll());
    }
}