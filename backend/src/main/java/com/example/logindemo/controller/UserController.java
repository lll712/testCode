package com.example.logindemo.controller;

import com.example.logindemo.dto.ApiResponse;
import com.example.logindemo.dto.UserUpdateRequest;
import com.example.logindemo.entity.User;
import com.example.logindemo.service.UserManagementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public ApiResponse<List<User>> list() {
        return ApiResponse.success("查询成功", userManagementService.list());
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success("更新成功", userManagementService.update(id, request));
    }

    @PutMapping("/{id}/disable")
    public ApiResponse<User> disable(@PathVariable Long id) {
        return ApiResponse.success("禁用成功", userManagementService.disable(id));
    }
}
