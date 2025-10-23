package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.service.UserService;
import com.sky.service.WeChatApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final WeChatApiService weChatApiService;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, WeChatApiService weChatApiService) {
        this.userMapper = userMapper;
        this.weChatApiService = weChatApiService;
    }

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String openid = weChatApiService.getOpenId(userLoginDTO.getCode());
        
        // 判断返回的openid是否为空，是空就抛出异常
        if (openid == null || openid.trim().isEmpty()) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        
        // 判断当前用户是否是新用户
        User user = userMapper.selectCountByOpenId(openid);
        
        // 如果是新用户就自动注册
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.saveNewUser(user);
        }
        
        return user;
    }
}
