package com.prabandhx.prabandhx.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String name;
    private String email;
    private String role;
    private Long organizationId;
}