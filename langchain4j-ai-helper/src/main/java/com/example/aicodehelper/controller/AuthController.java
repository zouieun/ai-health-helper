package com.example.aicodehelper.controller;

import com.example.aicodehelper.utils.JwtUtil;
import com.example.aicodehelper.common.Result;
import com.example.aicodehelper.pojo.dto.AuthDTO;
import com.example.aicodehelper.pojo.entity.User;
import com.example.aicodehelper.properties.JwtProperties;
import com.example.aicodehelper.service.LoginService;
import com.example.aicodehelper.pojo.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@Slf4j
public class AuthController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody AuthDTO authDTO) {
        log.info("login: {}", authDTO);
        User user = loginService.login(authDTO);
        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getSecretKey(),
                jwtProperties.getTtl(),
                claims);
        LoginVO loginVO = LoginVO.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .token(token)
                .build();
        return Result.success(loginVO);
    }
    @PostMapping("/register")
    public Result<Void> register(@RequestBody AuthDTO authDTO) {
        log.info("register: {}", authDTO.getUsername());
        loginService.register(authDTO);
        return Result.success();
    }
}
