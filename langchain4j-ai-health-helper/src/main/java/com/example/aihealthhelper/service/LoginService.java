package com.example.aihealthhelper.service;

import com.example.aihealthhelper.pojo.dto.AuthDTO;
import com.example.aihealthhelper.pojo.entity.User;

public interface LoginService {
    // 登录
    User login(AuthDTO authDTO);

    // 注册
    void register(AuthDTO authDTO);
}
