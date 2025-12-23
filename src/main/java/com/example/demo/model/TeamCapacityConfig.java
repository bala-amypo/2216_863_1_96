package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "team_capacity_configs")
public class TeamCapacityConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String teamName;

    @Column(nullable = false)
    private Integer totalHeadcount;

    @Column(nullable = false)
    private Integer minCapacityPercent;

    public TeamCapacityConfig() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public Integer getTotalHeadcount() { return totalHeadcount; }
    public void setTotalHeadcount(Integer totalHeadcount) { this.totalHeadcount = totalHeadcount; }

    public Integer getMinCapacityPercent() { return minCapacityPercent; }
    public void setMinCapacityPercent(Integer minCapacityPercent) { this.minCapacityPercent = minCapacityPercent; }
}