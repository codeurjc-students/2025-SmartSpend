package com.smartspend.transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {   

    List<Transaction> findByAccount_User_UserIdOrderByDateDesc(Long userId);

    @Query(value = "SELECT * FROM transactions WHERE account_id = :accountId ORDER BY date DESC, id DESC LIMIT :limit", nativeQuery = true)
    List<Transaction> findByAccountIdAndLimit(Long accountId, int limit);

    Page<Transaction> findByAccountIdOrderByDateDesc(Long accountId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) " +
           "FROM Transaction t WHERE t.account.id = :accountId AND t.date <= :endDate")
    BigDecimal findBalanceUpToDate(@Param("accountId") Long accountId, @Param("endDate") LocalDate endDate);

    // ✅ QUERIES OPTIMIZADAS PARA CHARTS  
    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.date BETWEEN :dateFrom AND :dateTo " + 
           "AND t.type = :type " +
           "ORDER BY t.date ASC")
    List<Transaction> findByAccountAndDateRangeAndType(
        @Param("accountId") Long accountId,
        @Param("dateFrom") LocalDate dateFrom, 
        @Param("dateTo") LocalDate dateTo,
        @Param("type") TransactionType type);

    // ✅ QUERY SÚPER OPTIMIZADA - SOLO TOTALES POR CATEGORÍA
    @Query("SELECT t.category.name, SUM(t.amount) " +
           "FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.date BETWEEN :dateFrom AND :dateTo " +
           "AND t.type = :type " +
           "GROUP BY t.category.name")
    List<Object[]> findCategoryTotalsByAccountAndDateRangeAndType(
        @Param("accountId") Long accountId,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo, 
        @Param("type") TransactionType type);

    // ✅ QUERY SÚPER SIMPLE PARA TOTALES DE INGRESOS/GASTOS
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.date BETWEEN :dateFrom AND :dateTo " +
           "AND t.type = :type")
    BigDecimal findTotalByAccountAndDateRangeAndType(
        @Param("accountId") Long accountId,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        @Param("type") TransactionType type);

       List<Transaction> findByIsRecurringSeriesParentTrueAndRecurrenceIsNotAndNextRecurrenceDateLessThanEqual(
        Recurrence recurrenceType, LocalDate today);

       @Query("SELECT t FROM Transaction t WHERE t.isRecurringSeriesParent = true " +
           "AND t.recurrence != 'NONE' " +
           "AND t.nextRecurrenceDate <= :today")
       List<Transaction> findPendingRecurringTransactions(@Param("today") LocalDate today);
}