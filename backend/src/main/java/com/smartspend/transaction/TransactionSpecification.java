package com.smartspend.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public class TransactionSpecification {
    
    public static Specification<Transaction> filterTransactions(
        Long accountId,
        String search,
        String type,
        LocalDate dateFrom,
        LocalDate dateTo,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Long categoryId) {


        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
        
            predicates.add(criteriaBuilder.equal(root.get("account").get("id"), accountId));
            
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            }

            if (type != null && !type.trim().isEmpty()) {
                try {
                    TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("type"), transactionType));
                } catch (IllegalArgumentException e) {
                    // Si el tipo no es válido, lo ignoramos
                }
            }
            
            if (dateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateFrom));
            }

            if (dateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateTo));
            }

            if (minAmount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }

            if (maxAmount != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            
            query.orderBy(criteriaBuilder.desc(root.get("date")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }



}
