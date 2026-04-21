package com.example.logindemo.service;

import com.example.logindemo.dto.LoginRequest;
import com.example.logindemo.dto.LoginResult;
import com.example.logindemo.entity.User;
import com.example.logindemo.exception.BusinessException;
import com.example.logindemo.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("当前账号已被禁用");
        }

        if (!StringUtils.hasText(user.getPassword()) || !user.getPassword().equals(request.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String nickname = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
        String token = "demo-token-" + UUID.randomUUID();
        return new LoginResult(user.getId(), user.getUsername(), nickname, token);
    }
}

