package com.example.logindemo.controller;

import com.example.logindemo.dto.ApiResponse;
import com.example.logindemo.dto.ReservationSaveRequest;
import com.example.logindemo.dto.ReservationView;
import com.example.logindemo.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ApiResponse<List<ReservationView>> list() {
        return ApiResponse.success("查询成功", reservationService.list());
    }

    @PostMapping
    public ApiResponse<ReservationView> create(@Valid @RequestBody ReservationSaveRequest request) {
        return ApiResponse.success("预约成功", reservationService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ReservationView> update(@PathVariable Long id, @Valid @RequestBody ReservationSaveRequest request) {
        return ApiResponse.success("修改成功", reservationService.update(id, request));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<ReservationView> cancel(@PathVariable Long id) {
        return ApiResponse.success("取消成功", reservationService.cancel(id));
    }
}
