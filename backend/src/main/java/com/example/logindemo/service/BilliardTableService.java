package com.example.logindemo.service;

import com.example.logindemo.dto.CloseTableRequest;
import com.example.logindemo.dto.TableSaveRequest;
import com.example.logindemo.entity.BilliardOrder;
import com.example.logindemo.entity.BilliardTable;
import com.example.logindemo.entity.User;
import com.example.logindemo.exception.BusinessException;
import com.example.logindemo.repository.BilliardOrderRepository;
import com.example.logindemo.repository.BilliardTableRepository;
import com.example.logindemo.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BilliardTableService {

    private final BilliardTableRepository billiardTableRepository;
    private final UserRepository userRepository;
    private final BilliardOrderRepository orderRepository;

    public BilliardTableService(
            BilliardTableRepository billiardTableRepository,
            UserRepository userRepository,
            BilliardOrderRepository orderRepository) {
        this.billiardTableRepository = billiardTableRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public List<BilliardTable> list() {
        return billiardTableRepository.findAll();
    }

    @Transactional
    public BilliardTable create(TableSaveRequest request) {
        billiardTableRepository.findByTableNo(request.getTableNo().trim()).ifPresent(table -> {
            throw new BusinessException("台球桌编号已存在");
        });

        BilliardTable table = new BilliardTable();
        apply(table, request);
        table.setCurrentUseStartTime(null);
        table.setCreateTime(LocalDateTime.now());
        table.setUpdateTime(LocalDateTime.now());
        return billiardTableRepository.save(table);
    }

    @Transactional
    public BilliardTable update(Long id, TableSaveRequest request) {
        BilliardTable table = getById(id);
        billiardTableRepository.findByTableNo(request.getTableNo().trim()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("台球桌编号已存在");
            }
        });

        apply(table, request);
        table.setUpdateTime(LocalDateTime.now());
        return billiardTableRepository.save(table);
    }

    @Transactional
    public void delete(Long id) {
        BilliardTable table = getById(id);
        if ("IN_USE".equals(table.getStatus())) {
            throw new BusinessException("使用中的台球桌不能删除");
        }
        billiardTableRepository.delete(table);
    }

    @Transactional
    public BilliardTable openTable(Long id) {
        BilliardTable table = getById(id);
        if ("MAINTENANCE".equals(table.getStatus())) {
            throw new BusinessException("维修中的台球桌不能开桌");
        }
        if ("IN_USE".equals(table.getStatus())) {
            throw new BusinessException("当前台球桌已在使用中");
        }

        table.setStatus("IN_USE");
        table.setCurrentUseStartTime(LocalDateTime.now());
        table.setUpdateTime(LocalDateTime.now());
        return billiardTableRepository.save(table);
    }

    @Transactional
    public BilliardOrder closeTable(Long id, CloseTableRequest request) {
        BilliardTable table = getById(id);
        if (!"IN_USE".equals(table.getStatus()) || table.getCurrentUseStartTime() == null) {
            throw new BusinessException("当前台球桌未开桌");
        }

        String lastFour = request.getMemberCardLastFour().trim();
        if (lastFour.length() != 4) {
            throw new BusinessException("会员卡后四位必须为 4 位");
        }

        List<User> matchedUsers = userRepository.findByMemberCardLastFour(lastFour).stream()
                .filter(user -> user.getStatus() != null && user.getStatus() == 1)
                .toList();
        if (matchedUsers.isEmpty()) {
            throw new BusinessException("未找到对应会员卡");
        }
        if (matchedUsers.size() > 1) {
            throw new BusinessException("存在多个会员卡后四位相同的用户，请维护唯一会员卡号");
        }

        User user = matchedUsers.get(0);
        LocalDateTime endTime = LocalDateTime.now();
        long durationMinutes = Math.max(Duration.between(table.getCurrentUseStartTime(), endTime).toMinutes(), 1);
        BigDecimal amount = calculateAmount(table.getHourlyPrice(), durationMinutes);
        BigDecimal balance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        if (balance.compareTo(amount) < 0) {
            throw new BusinessException("会员余额不足，当前余额：" + balance);
        }

        user.setBalance(balance.subtract(amount).setScale(2, RoundingMode.HALF_UP));
        user.setUpdateTime(endTime);
        userRepository.save(user);

        BilliardOrder order = new BilliardOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(user.getId());
        order.setTableId(table.getId());
        order.setReservationId(null);
        order.setStartTime(table.getCurrentUseStartTime());
        order.setEndTime(endTime);
        order.setDurationMinutes(durationMinutes);
        order.setHourlyPrice(table.getHourlyPrice());
        order.setAmount(amount);
        order.setPaymentStatus("PAID");
        order.setOrderStatus("FINISHED");
        order.setMemberCardLastFour(lastFour);
        order.setPaidTime(endTime);
        order.setRemark("台球桌开桌关桌自动结算");
        order.setCreateTime(endTime);
        order.setUpdateTime(endTime);
        BilliardOrder savedOrder = orderRepository.save(order);

        table.setStatus("IDLE");
        table.setCurrentUseStartTime(null);
        table.setUpdateTime(endTime);
        billiardTableRepository.save(table);

        return savedOrder;
    }

    public BilliardTable getById(Long id) {
        return billiardTableRepository.findById(id)
                .orElseThrow(() -> new BusinessException("台球桌不存在"));
    }

    private void apply(BilliardTable table, TableSaveRequest request) {
        table.setTableNo(request.getTableNo().trim());
        table.setTableName(request.getTableName().trim());
        table.setAreaName(StringUtils.hasText(request.getAreaName()) ? request.getAreaName().trim() : null);
        table.setStatus(request.getStatus().trim());
        table.setHourlyPrice(request.getHourlyPrice());
        table.setImageUrl(StringUtils.hasText(request.getImageUrl()) ? request.getImageUrl().trim() : null);
        table.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
    }

    private BigDecimal calculateAmount(BigDecimal hourlyPrice, Long durationMinutes) {
        BigDecimal ratio = BigDecimal.valueOf(durationMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        return hourlyPrice.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
