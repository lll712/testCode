package com.example.logindemo.repository;

import com.example.logindemo.entity.BilliardTable;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BilliardTableRepository extends JpaRepository<BilliardTable, Long> {

    Optional<BilliardTable> findByTableNo(String tableNo);
}
