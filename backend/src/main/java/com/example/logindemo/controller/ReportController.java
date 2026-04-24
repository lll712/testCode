package com.example.logindemo.controller;

import com.example.logindemo.dto.ApiResponse;
import com.example.logindemo.dto.ReportMetricItem;
import com.example.logindemo.service.ReportService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ApiResponse<List<ReportMetricItem>> summary(@RequestParam(defaultValue = "day") String type) {
        return ApiResponse.success("查询成功", reportService.summary(type));
    }
}
