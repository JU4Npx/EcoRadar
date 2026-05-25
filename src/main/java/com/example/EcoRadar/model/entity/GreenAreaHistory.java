package com.example.EcoRadar.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "green_area_history")
public class GreenAreaHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @ManyToOne
    @JoinColumn(name = "id_green_area")
    private GreenArea greenArea;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String data;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public GreenAreaHistory() {
    }

    public Integer getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }

    public GreenArea getGreenArea() {
        return greenArea;
    }

    public void setGreenArea(GreenArea greenArea) {
        this.greenArea = greenArea;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}