package com.example.logindemo.dto;

import java.math.BigDecimal;

public class ReportMetricItem {

    private String label;
    private Long totalMinutes;
    private BigDecimal usageRate;
    private BigDecimal income;
    private Long orderCount;

    public ReportMetricItem() {
    }

    public ReportMetricItem(String label, Long totalMinutes, BigDecimal usageRate, BigDecimal income, Long orderCount) {
        this.label = label;
        this.totalMinutes = totalMinutes;
        this.usageRate = usageRate;
        this.income = income;
        this.orderCount = orderCount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(Long totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public BigDecimal getUsageRate() {
        return usageRate;
    }

    public void setUsageRate(BigDecimal usageRate) {
        this.usageRate = usageRate;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }
}
