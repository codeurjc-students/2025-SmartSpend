package com.smartspend.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

@Service
public class CategoryService {

    private static final Set<String> INTERNAL_TRANSFER_CATEGORY_NAMES = Set.of(
        "Traspaso (Salida)",
        "Traspaso (Entrada)"
    );

    @Autowired
    UserRepository userRepository;

    @Autowired 
    CategoryRepository categoryRepository;


    public List<Category> getCategoriesForDropdown(String userEmail, TransactionType type) {
        
        User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        
        List<Category> systemCategories = new ArrayList<>(
            categoryRepository.findByIsDefaultTrueAndType(type)
        );
        systemCategories.removeIf(this::isInternalTransferCategory);
        
        
        List<Category> userCategories = categoryRepository.findByUserUserIdAndType(user.getUserId(), type);
        
        
        List<Category> allCategories = new ArrayList<>(systemCategories);
        allCategories.addAll(userCategories);
        
        return allCategories;
    }

    private boolean isInternalTransferCategory(Category category) {
        return Boolean.TRUE.equals(category.getIsDefault())
            && INTERNAL_TRANSFER_CATEGORY_NAMES.contains(category.getName());
    }
}
