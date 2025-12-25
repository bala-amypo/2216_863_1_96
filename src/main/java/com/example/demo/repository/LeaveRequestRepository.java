package com.example.demo.repository;

import com.example.demo.model.LeaveRequest;
import com.example.demo.model.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployee(EmployeeProfile employee);
    
    @Query("SELECT lr FROM LeaveRequest lr JOIN lr.employee e WHERE e.teamName = :teamName AND lr.status = 'APPROVED' AND lr.startDate <= :end AND lr.endDate >= :start")
    List<LeaveRequest> findApprovedOverlappingForTeam(@Param("teamName") String teamName, @Param("start") LocalDate start, @Param("end") LocalDate end);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'APPROVED' AND lr.startDate <= :date AND lr.endDate >= :date")
    List<LeaveRequest> findApprovedOnDate(@Param("date") LocalDate date);
}