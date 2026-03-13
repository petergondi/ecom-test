package com.example.demo.models.dtos;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String mobile;
    private String password;
}
