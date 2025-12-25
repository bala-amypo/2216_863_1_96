package com.example.demo.controller;

import com.example.demo.dto.LeaveRequestDto;
import com.example.demo.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leave Management", description = "Leave request management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class LeaveRequestController {

    private final LeaveRequestService leaveService;

    @Autowired
    public LeaveRequestController(LeaveRequestService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    @Operation(summary = "Submit leave request", description = "Submit a new leave request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave request submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid leave request data")
    })
    public ResponseEntity<LeaveRequestDto> createLeave(@RequestBody LeaveRequestDto dto) {
        return ResponseEntity.ok(leaveService.create(dto));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve leave request", description = "Approve a pending leave request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave request approved"),
        @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    public ResponseEntity<LeaveRequestDto> approveLeave(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.approve(id));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject leave request", description = "Reject a pending leave request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave request rejected"),
        @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    public ResponseEntity<LeaveRequestDto> rejectLeave(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.reject(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get employee leaves", description = "Get all leave requests for an employee")
    public ResponseEntity<List<LeaveRequestDto>> getLeavesByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getByEmployee(employeeId));
    }

    @GetMapping("/team-overlap")
    @Operation(summary = "Get overlapping leaves", description = "Get overlapping approved leaves for a team in date range")
    public ResponseEntity<List<LeaveRequestDto>> getOverlappingLeaves(
            @RequestParam String teamName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(leaveService.getOverlappingForTeam(teamName, start, end));
    }
}