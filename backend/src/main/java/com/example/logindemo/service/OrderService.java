package com.example.logindemo.service;

import com.example.logindemo.dto.OrderSaveRequest;
import com.example.logindemo.dto.OrderView;
import com.example.logindemo.entity.BilliardOrder;
import com.example.logindemo.entity.BilliardTable;
import com.example.logindemo.entity.Reservation;
import com.example.logindemo.entity.User;
import com.example.logindemo.exception.BusinessException;
import com.example.logindemo.repository.BilliardOrderRepository;
import com.example.logindemo.repository.BilliardTableRepository;
import com.example.logindemo.repository.ReservationRepository;
import com.example.logindemo.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OrderService {

    private final BilliardOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BilliardTableRepository billiardTableRepository;
    private final ReservationRepository reservationRepository;

    public OrderService(
            BilliardOrderRepository orderRepository,
            UserRepository userRepository,
            BilliardTableRepository billiardTableRepository,
            ReservationRepository reservationRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.billiardTableRepository = billiardTableRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<OrderView> list() {
        Map<Long, User> userMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, BilliardTable> tableMap = billiardTableRepository.findAll().stream()
                .collect(Collectors.toMap(BilliardTable::getId, Function.identity()));

        return orderRepository.findAllByOrderByStartTimeDesc().stream()
                .map(order -> toView(order, userMap.get(order.getUserId()), tableMap.get(order.getTableId())))
                .toList();
    }

    @Transactional
    public OrderView create(OrderSaveRequest request) {
        validateRequest(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        BilliardTable table = billiardTableRepository.findById(request.getTableId())
                .orElseThrow(() -> new BusinessException("台球桌不存在"));
        Reservation reservation = null;
        if (request.getReservationId() != null) {
            reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new BusinessException("关联预约不存在"));
        }

        BilliardOrder order = new BilliardOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(user.getId());
        order.setTableId(table.getId());
        order.setReservationId(reservation != null ? reservation.getId() : null);
        order.setStartTime(request.getStartTime());
        order.setEndTime(request.getEndTime());
        order.setDurationMinutes(Duration.between(request.getStartTime(), request.getEndTime()).toMinutes());
        order.setHourlyPrice(table.getHourlyPrice());
        order.setAmount(calculateAmount(table.getHourlyPrice(), order.getDurationMinutes()));
        order.setPaymentStatus(request.getPaymentStatus());
        order.setOrderStatus("FINISHED");
        order.setPaidTime("PAID".equals(request.getPaymentStatus()) ? LocalDateTime.now() : null);
        order.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        BilliardOrder saved = orderRepository.save(order);

        table.setStatus("IDLE");
        table.setCurrentUseStartTime(null);
        table.setUpdateTime(LocalDateTime.now());
        billiardTableRepository.save(table);

        return toView(saved, user, table);
    }

    @Transactional
    public OrderView markPaid(Long id) {
        BilliardOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        order.setPaymentStatus("PAID");
        order.setPaidTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        BilliardOrder saved = orderRepository.save(order);
        User user = userRepository.findById(saved.getUserId()).orElse(null);
        BilliardTable table = billiardTableRepository.findById(saved.getTableId()).orElse(null);
        return toView(saved, user, table);
    }

    private void validateRequest(OrderSaveRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("该用户已被禁用，不能创建订单");
        }

        BilliardTable table = billiardTableRepository.findById(request.getTableId())
                .orElseThrow(() -> new BusinessException("台球桌不存在"));
        if ("MAINTENANCE".equals(table.getStatus())) {
            throw new BusinessException("维修中的台球桌不能生成订单");
        }
    }

    private String generateOrderNo() {
        LocalDateTime now = LocalDateTime.now();
        int suffix = now.getNano() % 1000;
        return "ORD" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + String.format("%03d", suffix);
    }

    private BigDecimal calculateAmount(BigDecimal hourlyPrice, Long durationMinutes) {
        BigDecimal minutes = BigDecimal.valueOf(durationMinutes);
        BigDecimal ratio = minutes.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        return hourlyPrice.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
    }

    private OrderView toView(BilliardOrder order, User user, BilliardTable table) {
        OrderView view = new OrderView();
        view.setId(order.getId());
        view.setOrderNo(order.getOrderNo());
        view.setUserId(order.getUserId());
        view.setUsername(user != null ? user.getUsername() : "");
        view.setNickname(user != null ? user.getNickname() : "");
        view.setTableId(order.getTableId());
        view.setTableName(table != null ? table.getTableName() : "");
        view.setTableNo(table != null ? table.getTableNo() : "");
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
        return view;
    }
}
