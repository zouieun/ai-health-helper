package com.example.aihealthhelper.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO{
    private Long id;
    private String userName;
    private String token;
}
