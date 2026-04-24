package com.example.logindemo.repository;

import com.example.logindemo.entity.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByStartTimeDesc();

    List<Reservation> findByTableIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Long tableId, String status, LocalDateTime endTime, LocalDateTime startTime);
}
