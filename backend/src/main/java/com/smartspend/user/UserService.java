package com.smartspend.user;

import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.category.CategoryRepository;
import com.smartspend.transaction.DebtRepository;
import com.smartspend.transaction.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private DebtRepository debtRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private BankAccountRepository bankAccountRepository;
    @Autowired private CategoryRepository categoryRepository;

    @Transactional
    public void acceptPrivacyPolicy(String email) {
        User user = userRepository.findByUserEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + email));
        user.setPrivacyPolicyAccepted(true);
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByUserEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + email));
        Long userId = user.getUserId();
        // Borrado en cascada manual respetando integridad referencial
        debtRepository.deleteByTransaction_Account_User_UserId(userId);
        transactionRepository.deleteByAccount_User_UserId(userId);
        bankAccountRepository.deleteByUser_UserId(userId);
        categoryRepository.deleteByUser_UserId(userId);
        userRepository.delete(user);
    }
}
