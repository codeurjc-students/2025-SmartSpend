package com.smartspend.category;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartspend.transaction.TransactionType;

public interface CategoryRepository extends JpaRepository<Category, Long> {

   
    List<Category> findByIsDefaultTrueAndType(TransactionType type);
    
    
    List<Category> findByUserUserIdAndType(Long userId, TransactionType type);
    
   
    long countByIsDefaultTrue();

    List<Category> findByUserUserId(Long userId);
} 