package com.prabandhx.prabandhx.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProjectDTO {

    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long organizationId;
}