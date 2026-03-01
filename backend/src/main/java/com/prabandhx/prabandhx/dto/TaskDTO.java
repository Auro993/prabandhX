package com.prabandhx.prabandhx.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskDTO {

    private Long id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDate dueDate;
    private Long projectId;
    private Long assignedToUserId;
}