package com.example.logindemo.controller;

import com.example.logindemo.dto.ApiResponse;
import com.example.logindemo.dto.OrderSaveRequest;
import com.example.logindemo.dto.OrderView;
import com.example.logindemo.service.OrderService;
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
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<List<OrderView>> list() {
        return ApiResponse.success("查询成功", orderService.list());
    }

    @PostMapping
    public ApiResponse<OrderView> create(@Valid @RequestBody OrderSaveRequest request) {
        return ApiResponse.success("订单创建成功", orderService.create(request));
    }

    @PutMapping("/{id}/pay")
    public ApiResponse<OrderView> markPaid(@PathVariable Long id) {
        return ApiResponse.success("支付状态更新成功", orderService.markPaid(id));
    }
}
