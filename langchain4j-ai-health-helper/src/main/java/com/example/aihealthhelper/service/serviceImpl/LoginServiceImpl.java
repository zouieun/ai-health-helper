package com.example.aihealthhelper.service.serviceImpl;

import com.example.aihealthhelper.pojo.dto.AuthDTO;
import com.example.aihealthhelper.pojo.entity.User;
import com.example.aihealthhelper.mapper.LoginMapper;
import com.example.aihealthhelper.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import com.example.aihealthhelper.common.BusinessException;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private LoginMapper loginMapper;
    // 登录
    @Override
    public User login(AuthDTO authDTO) {
        String username = authDTO.getUsername();
        String password = authDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        User user = loginMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (user == null) {
            //账号不存在
            throw new BusinessException("账号不存在");
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            //密码错误
            throw new BusinessException("密码错误");
        }

        //3、返回实体对象
        return user;
    }

    // 注册
    @Override
    public void register(AuthDTO authDTO) {
        String username = authDTO.getUsername();
        String password = authDTO.getPassword();
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BusinessException("用户名或密码为空");
        }
        if (loginMapper.getByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }
        // TODO 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        loginMapper.register(username, password);
    }
}
