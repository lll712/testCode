package com.example.logindemo.repository;

import com.example.logindemo.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query(value = "select * from sys_user where right(member_card_no, 4) = :lastFour", nativeQuery = true)
    List<User> findByMemberCardLastFour(@Param("lastFour") String lastFour);
}
