package com.smartspend.transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    Optional<Debt> findByTransaction_Account_IdAndNameAndAmount(Long accountId, String name, BigDecimal amount);

    @Query("SELECT d FROM Debt d WHERE d.transaction.account.user.userId = :userId " +
           "AND d.isPaid = false ORDER BY d.transaction.date DESC, d.id DESC")
    List<Debt> findPendingDebtsByUserId(@Param("userId") Long userId, Pageable pageable);

    void deleteByTransaction_Account_User_UserId(Long userId);
}
