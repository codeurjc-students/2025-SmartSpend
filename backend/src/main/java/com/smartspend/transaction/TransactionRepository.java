package com.smartspend.transaction;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {   

    List<Transaction> findByAccount_User_UserIdOrderByDateDesc(Long userId);

    @Query(value = "SELECT * FROM transactions WHERE account_id = :accountId ORDER BY date DESC, id DESC LIMIT :limit", nativeQuery = true)
    List<Transaction> findByAccountIdAndLimit(Long accountId, int limit);

    Page<Transaction> findByAccountIdOrderByDateDesc(Long accountId, Pageable pageable);
}