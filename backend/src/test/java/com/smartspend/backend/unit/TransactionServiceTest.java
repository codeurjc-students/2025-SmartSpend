// package com.smartspend.backend.unit;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.Mockito.when;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import com.smartspend.category.Category;
// import com.smartspend.transaction.Recurrence;
// import com.smartspend.transaction.Transaction;
// import com.smartspend.transaction.TransactionRepository;
// import com.smartspend.transaction.TransactionService;
// import com.smartspend.transaction.TransactionType;
// import com.smartspend.user.User;
// import com.smartspend.bankAccount.BankAccount;

// public class TransactionServiceTest {
    
//     @Mock 
//     private TransactionRepository transactionRepository;

//     @InjectMocks
//     private TransactionService transactionService;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void returnTransactions(){
//         // Crear objetos necesarios para el test
//         User testUser = new User("testuser", "test@email.com", "hashedpassword");
        
//         BankAccount testAccount = new BankAccount(testUser, "Test Account", BigDecimal.ZERO);
        
//         Category testCategory = new Category("Nómina", "Ingresos por trabajo", "#27ae60", TransactionType.INCOME, null);
    
//         Transaction t1 = new Transaction(
//             "Nómina Septiembre", 
//             "Salario mensual", 
//             new BigDecimal("1200.00"), 
//             LocalDate.of(2025, 9, 1), 
//             TransactionType.INCOME, 
//             testCategory, 
//             Recurrence.NONE,
//             testAccount  // Añadido el BankAccount
//         );

//         when(transactionRepository.findAll()).thenReturn(List.of(t1));

//         List<Transaction> result = transactionService.findAll();

//         assertEquals(1, result.size());
//         assertEquals("Nómina Septiembre", result.get(0).getTitle());
//         assertEquals(TransactionType.INCOME, result.get(0).getType());
//         assertEquals(new BigDecimal("1200.00"), result.get(0).getAmount());
//     }
// }