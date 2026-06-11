package com.decisiontool.decisionflow.dtos;
import lombok.Data;
@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
}
