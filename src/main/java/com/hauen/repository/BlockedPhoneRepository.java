package com.hauen.repository;

import com.hauen.domain.BlockedPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockedPhoneRepository extends JpaRepository<BlockedPhone, Integer> {

    boolean existsByPhone(String phone);

    Optional<BlockedPhone> findByPhone(String phone);
}
