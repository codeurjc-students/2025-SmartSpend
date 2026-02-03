package com.smartspend.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.category.Category;
import com.smartspend.category.CategoryRepository;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.transaction.Transaction;
import com.smartspend.user.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;
 
    private final PasswordEncoder passwordEncoder;

    private final TransactionRepository transactionRepository;

    private final BankAccountRepository bankAccountRepository;

    


    @Override
    public void run(String... args) throws Exception {

        if (categoryRepository.count() == 0) {
            System.out.println("Cargando categorías iniciales por defecto...");

            
            userRepository.save(new User("admin", "administrator@gmail.com", "administrator"));
            


            // INCOMES CATEGORIES
            categoryRepository.save(new Category("Nómina", "Ingresos por trabajo", "#27ae60", TransactionType.INCOME, "💰"));
            categoryRepository.save(new Category("Venta", "Venta de productos/servicios", "#2ecc71", TransactionType.INCOME, "💸"));
            categoryRepository.save(new Category("Regalo", "Regalos o donaciones", "#3498db", TransactionType.INCOME, "🎁"));
            categoryRepository.save(new Category("Inversión", "Beneficios de inversiones", "#1abc9c", TransactionType.INCOME, "📈"));

            // EXPENSES CATEGORIES
            categoryRepository.save(new Category("Alimentación", "Compras de supermercado y comida", "#e74c3c", TransactionType.EXPENSE, "🛒"));
            categoryRepository.save(new Category("Transporte", "Gastos de coche, bus, tren", "#f39c12", TransactionType.EXPENSE, "🚌"));
            categoryRepository.save(new Category("Vivienda", "Alquiler, hipoteca, servicios", "#9b59b6", TransactionType.EXPENSE, "🏠"));
            categoryRepository.save(new Category("Ocio", "Entretenimiento, salidas", "#34495e", TransactionType.EXPENSE, "🎬"));
            categoryRepository.save(new Category("Facturas", "Electricidad, agua, internet, móvil", "#c0392b", TransactionType.EXPENSE, "🧾"));
            categoryRepository.save(new Category("Salud", "Médicos, medicinas, seguros", "#e67e22", TransactionType.EXPENSE, "🏥"));
            categoryRepository.save(new Category("Educación", "Cursos, libros, matrícula", "#2980b9", TransactionType.EXPENSE, "📚"));
            categoryRepository.save(new Category("Ropa", "Compras de vestimenta y accesorios", "#8e44ad", TransactionType.EXPENSE, "👕"));
            categoryRepository.save(new Category("Mascotas", "Comida, veterinario, accesorios", "#2c3e50", TransactionType.EXPENSE, "🐾"));
            categoryRepository.save(new Category("Viajes", "Vacaciones, billetes, alojamiento", "#16a085", TransactionType.EXPENSE, "✈️"));
            categoryRepository.save(new Category("Otros", "Gastos no clasificados", "#7f8c8d", TransactionType.EXPENSE, "❓"));

            System.out.println("Categorías iniciales cargadas.");
        } else {
            System.out.println("Categories already exist, skipping initial category loading.");
        }

        String adminEmail = "administrator@gmail.com";
        if (userRepository.findByUserEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setUserName("admin");
            admin.setUserEmail(adminEmail);
            admin.setUserHashedPassword(passwordEncoder.encode("administrator"));
            userRepository.save(admin);
        } else {
            System.out.println("Admin user already exists, skipping admin user creation.");
        }

        User admin = userRepository.findByUserEmail(adminEmail).orElse(null);
        System.out.println("Admin user: " + admin);
        if (admin != null && bankAccountRepository.countByUser(admin) == 0) {
            // Crear cuenta bancaria
            BankAccount demoAccount = new BankAccount(admin, "Cuenta Principal", new BigDecimal("1000.00"));
            bankAccountRepository.save(demoAccount);
            System.out.println("Cuenta bancaria de ejemplo creada.");

            // Obtener categorías para asociar a las transacciones
            Category nomina = categoryRepository.findByName("Nómina");
            Category alimentacion = categoryRepository.findByName("Alimentación");
            Category ocio = categoryRepository.findByName("Ocio");

            // Crear transacciones de ejemplo
            if (nomina != null) {
                transactionRepository.save(Transaction.builder()
                    .title("Sueldo Junio")
                    .description("Nómina de junio")
                    .amount(new BigDecimal("1500.00"))
                    .date(LocalDate.now().minusDays(10))
                    .type(TransactionType.INCOME)
                    .recurrence(com.smartspend.transaction.Recurrence.NONE)
                    .category(nomina)
                    .account(demoAccount)
                    .build());
            }
            if (alimentacion != null) {
                transactionRepository.save(Transaction.builder()
                    .title("Compra supermercado")
                    .description("Compra semanal")
                    .amount(new BigDecimal("80.50"))
                    .date(LocalDate.now().minusDays(5))
                    .type(TransactionType.EXPENSE)
                    .recurrence(com.smartspend.transaction.Recurrence.NONE)
                    .category(alimentacion)
                    .account(demoAccount)
                    .build());
            }
            if (ocio != null) {
                transactionRepository.save(Transaction.builder()
                    .title("Cine")
                    .description("Entradas para el cine")
                    .amount(new BigDecimal("20.00"))
                    .date(LocalDate.now().minusDays(2))
                    .type(TransactionType.EXPENSE)
                    .recurrence(com.smartspend.transaction.Recurrence.NONE)
                    .category(ocio)
                    .account(demoAccount)
                    .build());
            }

            System.out.println("Cuenta y transacciones de ejemplo creadas.");
        }


        

    }
}