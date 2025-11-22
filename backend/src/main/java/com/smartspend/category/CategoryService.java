package com.smartspend.category;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

@Service
public class CategoryService {

    @Autowired
    UserRepository userRepository;

    @Autowired 
    CategoryRepository categoryRepository;


    public List<Category> getCategoriesForDropdown(String userEmail, TransactionType type) {
        
        User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        
        List<Category> systemCategories = categoryRepository.findByIsDefaultTrueAndType(type);
        
        
        List<Category> userCategories = categoryRepository.findByUserUserIdAndType(user.getUserId(), type);
        
        
        List<Category> allCategories = new ArrayList<>(systemCategories);
        allCategories.addAll(userCategories);
        
        return allCategories;
    }
}
