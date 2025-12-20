package com.example.demo.repository;

import com.example.demo.model.TeamCapacityConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TeamCapacityConfigRepository extends JpaRepository<TeamCapacityConfig, Long> {
    Optional<TeamCapacityConfig> findByTeamName(String teamName);
}