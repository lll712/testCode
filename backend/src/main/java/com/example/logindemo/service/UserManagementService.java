package com.example.logindemo.service;

import com.example.logindemo.dto.UserUpdateRequest;
import com.example.logindemo.entity.User;
import com.example.logindemo.exception.BusinessException;
import com.example.logindemo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserManagementService {

    private final UserRepository userRepository;

    public UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> list() {
        return userRepository.findAll();
    }

    @Transactional
    public User update(Long id, UserUpdateRequest request) {
        User user = getById(id);
        user.setNickname(request.getNickname().trim());
        user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        user.setMemberCardNo(StringUtils.hasText(request.getMemberCardNo()) ? request.getMemberCardNo().trim() : null);
        user.setBalance(request.getBalance());
        user.setStatus(request.getStatus());
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User disable(Long id) {
        User user = getById(id);
        user.setStatus(0);
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    private User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}
