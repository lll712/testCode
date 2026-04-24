package com.example.logindemo.repository;

import com.example.logindemo.entity.BilliardOrder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BilliardOrderRepository extends JpaRepository<BilliardOrder, Long> {

    List<BilliardOrder> findAllByOrderByStartTimeDesc();

    Optional<BilliardOrder> findByOrderNo(String orderNo);

    List<BilliardOrder> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
