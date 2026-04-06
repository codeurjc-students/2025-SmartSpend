package com.smartspend.transaction;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    Optional<Debt> findByTransaction_Account_IdAndNameAndAmount(Long accountId, String name, BigDecimal amount);
}
