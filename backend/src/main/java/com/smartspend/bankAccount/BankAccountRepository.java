package com.smartspend.bankAccount;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long>{
    List<BankAccount> findByUser_UserId(Long userId);
    Optional<BankAccount> findByIdAndUser_UserId(Long id, Long userId);
}
