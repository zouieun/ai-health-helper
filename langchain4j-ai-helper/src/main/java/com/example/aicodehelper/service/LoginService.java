package com.example.aicodehelper.service;

import com.example.aicodehelper.pojo.dto.AuthDTO;
import com.example.aicodehelper.pojo.entity.User;

public interface LoginService {
    // 登录
    User login(AuthDTO authDTO);

    // 注册
    void register(AuthDTO authDTO);
}
