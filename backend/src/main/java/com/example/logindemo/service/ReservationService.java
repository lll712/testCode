package com.example.logindemo.service;

import com.example.logindemo.dto.ReservationSaveRequest;
import com.example.logindemo.dto.ReservationView;
import com.example.logindemo.entity.BilliardTable;
import com.example.logindemo.entity.Reservation;
import com.example.logindemo.entity.User;
import com.example.logindemo.exception.BusinessException;
import com.example.logindemo.repository.BilliardTableRepository;
import com.example.logindemo.repository.ReservationRepository;
import com.example.logindemo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ReservationService {

    private static final String STATUS_BOOKED = "BOOKED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final BilliardTableRepository billiardTableRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            BilliardTableRepository billiardTableRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.billiardTableRepository = billiardTableRepository;
    }

    public List<ReservationView> list() {
        Map<Long, User> userMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, BilliardTable> tableMap = billiardTableRepository.findAll().stream()
                .collect(Collectors.toMap(BilliardTable::getId, Function.identity()));

        return reservationRepository.findAllByOrderByStartTimeDesc().stream()
                .sorted(Comparator.comparing(Reservation::getStartTime).reversed())
                .map(item -> toView(item, userMap.get(item.getUserId()), tableMap.get(item.getTableId())))
                .toList();
    }

    @Transactional
    public ReservationView create(ReservationSaveRequest request) {
        validateRequest(request, null);

        Reservation reservation = new Reservation();
        reservation.setStatus(STATUS_BOOKED);
        reservation.setCreateTime(LocalDateTime.now());
        reservation.setUpdateTime(LocalDateTime.now());
        apply(reservation, request);
        Reservation saved = reservationRepository.save(reservation);
        return buildView(saved);
    }

    @Transactional
    public ReservationView update(Long id, ReservationSaveRequest request) {
        Reservation reservation = getById(id);
        if (STATUS_CANCELLED.equals(reservation.getStatus())) {
            throw new BusinessException("已取消预约不能修改");
        }

        validateRequest(request, id);
        reservation.setUpdateTime(LocalDateTime.now());
        apply(reservation, request);
        Reservation saved = reservationRepository.save(reservation);
        return buildView(saved);
    }

    @Transactional
    public ReservationView cancel(Long id) {
        Reservation reservation = getById(id);
        reservation.setStatus(STATUS_CANCELLED);
        reservation.setUpdateTime(LocalDateTime.now());
        return buildView(reservationRepository.save(reservation));
    }

    private void validateRequest(ReservationSaveRequest request, Long currentId) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("预约用户不存在"));
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("该用户已被禁用，不能预约");
        }

        BilliardTable table = billiardTableRepository.findById(request.getTableId())
                .orElseThrow(() -> new BusinessException("台球桌不存在"));
        if (!"IDLE".equals(table.getStatus())) {
            throw new BusinessException("当前台球桌不是空闲状态，不能预约");
        }

        List<Reservation> conflicts = reservationRepository.findByTableIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                request.getTableId(), STATUS_BOOKED, request.getEndTime(), request.getStartTime());
        boolean hasConflict = conflicts.stream().anyMatch(item -> currentId == null || !item.getId().equals(currentId));
        if (hasConflict) {
            throw new BusinessException("预约时间与现有预约冲突");
        }
    }

    private void apply(Reservation reservation, ReservationSaveRequest request) {
        reservation.setUserId(request.getUserId());
        reservation.setTableId(request.getTableId());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setContactPhone(StringUtils.hasText(request.getContactPhone()) ? request.getContactPhone().trim() : null);
        reservation.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
    }

    private ReservationView buildView(Reservation reservation) {
        User user = userRepository.findById(reservation.getUserId()).orElse(null);
        BilliardTable table = billiardTableRepository.findById(reservation.getTableId()).orElse(null);
        return toView(reservation, user, table);
    }

    private ReservationView toView(Reservation reservation, User user, BilliardTable table) {
        ReservationView view = new ReservationView();
        view.setId(reservation.getId());
        view.setUserId(reservation.getUserId());
        view.setUsername(user != null ? user.getUsername() : "");
        view.setNickname(user != null ? user.getNickname() : "");
        view.setTableId(reservation.getTableId());
        view.setTableName(table != null ? table.getTableName() : "");
        view.setTableNo(table != null ? table.getTableNo() : "");
        view.setStartTime(reservation.getStartTime());
        view.setEndTime(reservation.getEndTime());
        view.setContactPhone(reservation.getContactPhone());
        view.setStatus(reservation.getStatus());
        view.setRemark(reservation.getRemark());
        view.setCreateTime(reservation.getCreateTime());
        return view;
    }

    private Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预约记录不存在"));
    }
}
