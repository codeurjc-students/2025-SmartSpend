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

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId AND t.title = :title AND t.amount = :amount AND t.type = 'INCOME' AND t.excludeFromStats = true")
    List<Transaction> findAdjustments(@Param("accountId") Long accountId, @Param("title") String title, @Param("amount") BigDecimal amount);

    List<Transaction> findByAccount_User_UserIdOrderByDateDesc(Long userId);

    @Query(value = "SELECT * FROM transactions WHERE account_id = :accountId ORDER BY date DESC, id DESC LIMIT :limit", nativeQuery = true)
    List<Transaction> findByAccountIdAndLimit(Long accountId, int limit);

    Page<Transaction> findByAccountIdOrderByDateDesc(Long accountId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) " +
           "FROM Transaction t WHERE t.account.id = :accountId AND t.date <= :endDate")
    BigDecimal findBalanceUpToDate(@Param("accountId") Long accountId, @Param("endDate") LocalDate endDate);

       @Query("SELECT DISTINCT t FROM Transaction t WHERE t.account.id = :accountId " +
              "AND t.isRecurringSeriesParent = true " +
              "AND (" +
              "    t.nextRecurrenceDate BETWEEN :dateFrom AND :dateTo " +
              "    OR EXISTS (" +
              "        SELECT c FROM Transaction c " +
              "        WHERE c.parentTransaction = t " +
              "        AND c.date BETWEEN :dateFrom AND :dateTo" +
              "    )" +
              ") " +
              "ORDER BY t.nextRecurrenceDate ASC")
       List<Transaction> findRecurringOrFixedByAccountAndDateRange(
              @Param("accountId") Long accountId,
              @Param("dateFrom") LocalDate dateFrom,
              @Param("dateTo") LocalDate dateTo);

       default List<Transaction> findRecurringOrFixedCurrentMonthByAccount(Long accountId) {
              LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
              LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
              return findRecurringOrFixedByAccountAndDateRange(accountId, firstDayOfMonth, lastDayOfMonth);
       }

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
    @Query("SELECT t.category.name, SUM(COALESCE(t.effectiveAmount, t.amount)) " +
           "FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.date BETWEEN :dateFrom AND :dateTo " +
           "AND t.type = :type " +
           "AND (t.excludeFromStats IS NULL OR t.excludeFromStats = false) " +
           "GROUP BY t.category.name")
    List<Object[]> findCategoryTotalsByAccountAndDateRangeAndType(
        @Param("accountId") Long accountId,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo, 
        @Param("type") TransactionType type);

    // ✅ QUERY SÚPER SIMPLE PARA TOTALES DE INGRESOS/GASTOS
    @Query("SELECT SUM(COALESCE(t.effectiveAmount, t.amount)) FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.date BETWEEN :dateFrom AND :dateTo " +
           "AND t.type = :type " +
           "AND (t.excludeFromStats IS NULL OR t.excludeFromStats = false)")
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

       @Query("SELECT DISTINCT t FROM Transaction t " +
              "LEFT JOIN FETCH t.childTransactions c " +
              "WHERE t.account.id = :accountId AND t.isRecurringSeriesParent = true " +
              "ORDER BY t.nextRecurrenceDate ASC")
       List<Transaction> findRecurringParentsWithChildrenByAccountId(@Param("accountId") Long accountId);
}
