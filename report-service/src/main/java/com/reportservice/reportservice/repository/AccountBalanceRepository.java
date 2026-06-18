package com.reportservice.reportservice.repository;

import com.reportservice.reportservice.entity.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, UUID> {
    @Modifying
    @Query(value = """
            INSERT INTO account_balance (user_id, balance, updated_at)
            VALUES (:userId, :delta, now())
            ON CONFLICT (user_id)
            DO UPDATE SET balance = account_balance.balance + :delta, updated_at = now()
            """, nativeQuery = true)
    void applyDelta(@Param("userId") UUID userId, @Param("delta") BigDecimal delta);

    void findAllByUserId(UUID userId);
}