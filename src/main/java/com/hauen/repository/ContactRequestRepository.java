package com.hauen.repository;

import com.hauen.domain.ContactRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ContactRequestRepository extends JpaRepository<ContactRequest, Integer> {

    @Query("SELECT COUNT(c) FROM ContactRequest c WHERE c.clientIp = :ip AND c.createdAt > :since")
    long countByClientIpSince(@Param("ip") String ip, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(c) FROM ContactRequest c WHERE c.phone = :phone AND c.createdAt > :since")
    long countByPhoneSince(@Param("phone") String phone, @Param("since") LocalDateTime since);
}
