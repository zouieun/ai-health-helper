package com.example.aicodehelper.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuthDTO implements Serializable {
    private String username;
    private String password;
}
