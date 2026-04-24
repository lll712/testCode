package com.example.logindemo.controller;

import com.example.logindemo.dto.ApiResponse;
import com.example.logindemo.dto.CloseTableRequest;
import com.example.logindemo.dto.OrderView;
import com.example.logindemo.dto.TableSaveRequest;
import com.example.logindemo.entity.BilliardOrder;
import com.example.logindemo.entity.BilliardTable;
import com.example.logindemo.service.BilliardTableService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tables")
public class BilliardTableController {

    private final BilliardTableService billiardTableService;

    public BilliardTableController(BilliardTableService billiardTableService) {
        this.billiardTableService = billiardTableService;
    }

    @GetMapping
    public ApiResponse<List<BilliardTable>> list() {
        return ApiResponse.success("查询成功", billiardTableService.list());
    }

    @PostMapping
    public ApiResponse<BilliardTable> create(@Valid @RequestBody TableSaveRequest request) {
        return ApiResponse.success("新增成功", billiardTableService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BilliardTable> update(@PathVariable Long id, @Valid @RequestBody TableSaveRequest request) {
        return ApiResponse.success("更新成功", billiardTableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        billiardTableService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/open")
    public ApiResponse<BilliardTable> open(@PathVariable Long id) {
        return ApiResponse.success("开桌成功", billiardTableService.openTable(id));
    }

    @PutMapping("/{id}/close")
    public ApiResponse<OrderView> close(@PathVariable Long id, @Valid @RequestBody CloseTableRequest request) {
        BilliardOrder order = billiardTableService.closeTable(id, request);
        OrderView view = new OrderView();
        view.setId(order.getId());
        view.setOrderNo(order.getOrderNo());
        view.setUserId(order.getUserId());
        view.setTableId(order.getTableId());
        view.setReservationId(order.getReservationId());
        view.setStartTime(order.getStartTime());
        view.setEndTime(order.getEndTime());
        view.setDurationMinutes(order.getDurationMinutes());
        view.setHourlyPrice(order.getHourlyPrice());
        view.setAmount(order.getAmount());
        view.setPaymentStatus(order.getPaymentStatus());
        view.setOrderStatus(order.getOrderStatus());
        view.setMemberCardLastFour(order.getMemberCardLastFour());
        view.setPaidTime(order.getPaidTime());
        view.setRemark(order.getRemark());
        view.setCreateTime(order.getCreateTime());
        return ApiResponse.success("关桌结算成功", view);
    }
}
