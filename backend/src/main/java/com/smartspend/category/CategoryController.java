package com.smartspend.category;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartspend.transaction.TransactionType;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getCategories(@RequestParam TransactionType type, Authentication authentication) {
        
        String userEmail = authentication.getName();
        List<Category> categories = categoryService.getCategoriesForDropdown(userEmail, type);
        return ResponseEntity.ok(categories);
    }
}
