package com.example.aihealthhelper.mapper;

import com.example.aihealthhelper.pojo.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LoginMapper {
    // 登录
    @Select("SELECT * FROM user WHERE username = #{username}")
    User getByUsername(String username);

    // 注册
    @Insert("INSERT INTO user (username, password) VALUES (#{username}, #{password})")
    void register(String username, String password);
}
