package com.example.logindemo.service;

import com.example.logindemo.dto.ReportMetricItem;
import com.example.logindemo.entity.BilliardOrder;
import com.example.logindemo.repository.BilliardOrderRepository;
import com.example.logindemo.repository.BilliardTableRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final BilliardOrderRepository orderRepository;
    private final BilliardTableRepository billiardTableRepository;

    public ReportService(BilliardOrderRepository orderRepository, BilliardTableRepository billiardTableRepository) {
        this.orderRepository = orderRepository;
        this.billiardTableRepository = billiardTableRepository;
    }

    public List<ReportMetricItem> summary(String type) {
        int tableCount = Math.max(billiardTableRepository.findAll().size(), 1);
        return switch (type == null ? "day" : type.toLowerCase()) {
            case "week" -> buildWeekly(tableCount);
            case "month" -> buildMonthly(tableCount);
            default -> buildDaily(tableCount);
        };
    }

    private List<ReportMetricItem> buildDaily(int tableCount) {
        List<ReportMetricItem> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            result.add(buildItem(
                    day.toString(),
                    day.atStartOfDay(),
                    day.atTime(LocalTime.MAX),
                    tableCount,
                    24L * 60));
        }
        return result;
    }

    private List<ReportMetricItem> buildWeekly(int tableCount) {
        List<ReportMetricItem> result = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusWeeks(i);
            LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate weekEnd = weekStart.plusDays(6);
            result.add(buildItem(
                    weekStart + " ~ " + weekEnd,
                    weekStart.atStartOfDay(),
                    weekEnd.atTime(LocalTime.MAX),
                    tableCount,
                    7L * 24 * 60));
        }
        return result;
    }

    private List<ReportMetricItem> buildMonthly(int tableCount) {
        List<ReportMetricItem> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = date.withDayOfMonth(date.lengthOfMonth());
            long monthMinutes = (long) date.lengthOfMonth() * 24 * 60;
            result.add(buildItem(
                    date.getYear() + "-" + String.format("%02d", date.getMonthValue()),
                    date.atStartOfDay(),
                    monthEnd.atTime(LocalTime.MAX),
                    tableCount,
                    monthMinutes));
        }
        return result;
    }

    private ReportMetricItem buildItem(
            String label,
            LocalDateTime start,
            LocalDateTime end,
            int tableCount,
            long baseMinutes) {
        List<BilliardOrder> orders = orderRepository.findByStartTimeBetween(start, end);
        long totalMinutes = orders.stream().mapToLong(BilliardOrder::getDurationMinutes).sum();
        BigDecimal income = orders.stream()
                .map(BilliardOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal denominator = BigDecimal.valueOf((long) tableCount * baseMinutes);
        BigDecimal usageRate = denominator.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalMinutes)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(denominator, 2, RoundingMode.HALF_UP);
        return new ReportMetricItem(label, totalMinutes, usageRate, income, (long) orders.size());
    }
}
