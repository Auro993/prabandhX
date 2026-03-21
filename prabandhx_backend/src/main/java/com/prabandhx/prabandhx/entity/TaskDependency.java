package com.prabandhx.prabandhx.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_dependencies")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TaskDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predecessor_task_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "incomingDependencies", "outgoingDependencies"})
    private Task predecessorTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "successor_task_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "incomingDependencies", "outgoingDependencies"})
    private Task successorTask;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false)
    private DependencyType dependencyType = DependencyType.FINISH_TO_START;

    @Column(name = "lag_days")
    private Integer lagDays = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DependencyType {
        FINISH_TO_START,  // Task B cannot start until Task A finishes
        START_TO_START,   // Task B cannot start until Task A starts
        FINISH_TO_FINISH, // Task B cannot finish until Task A finishes
        START_TO_FINISH   // Task B cannot finish until Task A starts
    }

    // ===== CONSTRUCTORS =====
    
    public TaskDependency() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public TaskDependency(Task predecessorTask, Task successorTask, DependencyType dependencyType) {
        this.predecessorTask = predecessorTask;
        this.successorTask = successorTask;
        this.dependencyType = dependencyType;
        this.lagDays = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public TaskDependency(Task predecessorTask, Task successorTask, DependencyType dependencyType, Integer lagDays) {
        this.predecessorTask = predecessorTask;
        this.successorTask = successorTask;
        this.dependencyType = dependencyType;
        this.lagDays = lagDays;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ===== GETTERS & SETTERS =====
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getPredecessorTask() {
        return predecessorTask;
    }

    public void setPredecessorTask(Task predecessorTask) {
        this.predecessorTask = predecessorTask;
    }

    public Task getSuccessorTask() {
        return successorTask;
    }

    public void setSuccessorTask(Task successorTask) {
        this.successorTask = successorTask;
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public Integer getLagDays() {
        return lagDays;
    }

    public void setLagDays(Integer lagDays) {
        this.lagDays = lagDays;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ===== HELPER METHODS =====
    
    public Long getPredecessorTaskId() {
        return predecessorTask != null ? predecessorTask.getId() : null;
    }

    public Long getSuccessorTaskId() {
        return successorTask != null ? successorTask.getId() : null;
    }

    public String getPredecessorTaskTitle() {
        return predecessorTask != null ? predecessorTask.getTitle() : null;
    }

    public String getSuccessorTaskTitle() {
        return successorTask != null ? successorTask.getTitle() : null;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "TaskDependency{" +
                "id=" + id +
                ", predecessorTaskId=" + getPredecessorTaskId() +
                ", successorTaskId=" + getSuccessorTaskId() +
                ", dependencyType=" + dependencyType +
                ", lagDays=" + lagDays +
                '}';
    }
}