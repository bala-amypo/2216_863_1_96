package com.example.demo.controller;

import com.example.demo.dto.LeaveRequestDto;
import com.example.demo.service.LeaveRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {
    
    private final LeaveRequestService leaveService;
    
    public LeaveRequestController(LeaveRequestService leaveService) {
        this.leaveService = leaveService;
    }
    
    @PostMapping
    public ResponseEntity<LeaveRequestDto> create(@RequestBody LeaveRequestDto dto) {
        LeaveRequestDto created = leaveService.create(dto);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestDto> approve(@PathVariable Long id) {
        LeaveRequestDto approved = leaveService.approve(id);
        return ResponseEntity.ok(approved);
    }
    
    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestDto> reject(@PathVariable Long id) {
        LeaveRequestDto rejected = leaveService.reject(id);
        return ResponseEntity.ok(rejected);
    }
    
    @GetMapping("/employee/{id}")
    public ResponseEntity<List<LeaveRequestDto>> getByEmployee(@PathVariable Long id) {
        List<LeaveRequestDto> leaves = leaveService.getByEmployee(id);
        return ResponseEntity.ok(leaves);
    }
}