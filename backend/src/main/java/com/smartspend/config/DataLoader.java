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

@Component
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;
 
    private final PasswordEncoder passwordEncoder;

    private final TransactionRepository transactionRepository;

    private final BankAccountRepository bankAccountRepository;

    public DataLoader(CategoryRepository categoryRepository, UserRepository userRepository, 
                     PasswordEncoder passwordEncoder, TransactionRepository transactionRepository, 
                     BankAccountRepository bankAccountRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    


    @Override
    public void run(String... args) throws Exception {

        // Limpiar datos existentes para empezar desde cero (orden importante por foreign keys)
        System.out.println("🧹 Limpiando datos existentes...");
        transactionRepository.deleteAll();  // 1. Primero transacciones (dependen de accounts y categories)
        bankAccountRepository.deleteAll();  // 2. Luego bank accounts (dependen de users)  
        categoryRepository.deleteAll();     // 3. Categorías (independientes)
        userRepository.deleteAll();         // 4. Finalmente usuarios (tabla padre)
        

        System.out.println("📦 Cargando categorías iniciales por defecto...");

        // INCOMES CATEGORIES
        Category nomina = categoryRepository.save(new Category("Nómina", "Ingresos por trabajo", "#27ae60", TransactionType.INCOME, "💰"));
        Category freelance = categoryRepository.save(new Category("Freelance", "Trabajos independientes", "#16a085", TransactionType.INCOME, "💼"));
        Category venta = categoryRepository.save(new Category("Ventas", "Venta de productos/servicios", "#2ecc71", TransactionType.INCOME, "💸"));
        Category regalo = categoryRepository.save(new Category("Regalos", "Regalos o donaciones", "#3498db", TransactionType.INCOME, "🎁"));
        Category inversion = categoryRepository.save(new Category("Inversiones", "Beneficios de inversiones", "#1abc9c", TransactionType.INCOME, "📈"));

        // EXPENSES CATEGORIES
        Category alimentacion = categoryRepository.save(new Category("Comida", "Compras de supermercado y comida", "#e74c3c", TransactionType.EXPENSE, "🛒"));
        Category transporte = categoryRepository.save(new Category("Transporte", "Gastos de coche, bus, tren", "#f39c12", TransactionType.EXPENSE, "🚌"));
        Category vivienda = categoryRepository.save(new Category("Vivienda", "Alquiler, hipoteca, servicios", "#9b59b6", TransactionType.EXPENSE, "🏠"));
        Category ocio = categoryRepository.save(new Category("Ocio", "Entretenimiento, salidas", "#34495e", TransactionType.EXPENSE, "🎬"));
        Category facturas = categoryRepository.save(new Category("Facturas", "Electricidad, agua, internet, móvil", "#c0392b", TransactionType.EXPENSE, "🧾"));
        Category salud = categoryRepository.save(new Category("Salud", "Médicos, medicinas, seguros", "#e67e22", TransactionType.EXPENSE, "🏥"));
        Category educacion = categoryRepository.save(new Category("Educación", "Cursos, libros, matrícula", "#2980b9", TransactionType.EXPENSE, "📚"));
        Category ropa = categoryRepository.save(new Category("Ropa", "Compras de vestimenta y accesorios", "#8e44ad", TransactionType.EXPENSE, "👕"));
        Category mascotas = categoryRepository.save(new Category("Mascotas", "Comida, veterinario, accesorios", "#2c3e50", TransactionType.EXPENSE, "🐾"));
        Category viajes = categoryRepository.save(new Category("Viajes", "Vacaciones, billetes, alojamiento", "#16a085", TransactionType.EXPENSE, "✈️"));
        Category otros = categoryRepository.save(new Category("Otros", "Gastos no clasificados", "#7f8c8d", TransactionType.EXPENSE, "❓"));

        System.out.println("✅ Categorías iniciales cargadas: " + categoryRepository.count() + " categorías");

        // CREAR USUARIO ADMIN
        User admin = new User();
        admin.setUserName("admin");
        admin.setUserEmail("administrator@gmail.com");
        admin.setUserHashedPassword(passwordEncoder.encode("administrator"));
        admin = userRepository.save(admin);
        System.out.println("👤 Usuario admin creado: " + admin.getUserEmail());

        // CREAR CUENTA BANCARIA
        BankAccount demoAccount = new BankAccount(admin, "Cuenta Principal", new BigDecimal("1000.00"));
        demoAccount = bankAccountRepository.save(demoAccount);
        System.out.println("🏦 Cuenta bancaria creada: " + demoAccount.getAccountName());

        System.out.println("📊 Creando transacciones de prueba...");

            // ========== INGRESOS DE LOS ÚLTIMOS MESES ==========
            
            // Enero 2026
            transactionRepository.save(Transaction.builder()
                .title("Nómina Enero")
                .description("Sueldo de enero 2026")
                .amount(new BigDecimal("2500.00"))
                .date(LocalDate.of(2026, 1, 31))
                .type(TransactionType.INCOME)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(nomina)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Nómina Enero - 2500.00€");
            
            transactionRepository.save(Transaction.builder()
                .title("Venta artículos usados")
                .description("Venta en Wallapop")
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.of(2026, 1, 15))
                .type(TransactionType.INCOME)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(venta)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Venta artículos usados - 150.00€");
            
            transactionRepository.save(Transaction.builder()
                .title("Regalo cumpleaños")
                .description("Dinero de cumpleaños")
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 1, 20))
                .type(TransactionType.INCOME)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(regalo)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Regalo cumpleaños - 100.00€");

            // Febrero 2026 (mes actual)
            transactionRepository.save(Transaction.builder()
                .title("Nómina Febrero")
                .description("Sueldo de febrero 2026")
                .amount(new BigDecimal("2600.00"))
                .date(LocalDate.of(2026, 2, 5))
                .type(TransactionType.INCOME)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(nomina)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Nómina Febrero - 2600.00€");

            transactionRepository.save(Transaction.builder()
                .title("Dividendos")
                .description("Dividendos de acciones")
                .amount(new BigDecimal("75.50"))
                .date(LocalDate.of(2026, 2, 8))
                .type(TransactionType.INCOME)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(inversion)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Dividendos - 75.50€");

            // ========== GASTOS ENERO 2026 ==========
            
            // Múltiples compras de supermercado
            transactionRepository.save(Transaction.builder()
                .title("Mercadona")
                .description("Compra semanal")
                .amount(new BigDecimal("95.30"))
                .date(LocalDate.of(2026, 1, 7))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(alimentacion)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Mercadona - 95.30€");
                
            transactionRepository.save(Transaction.builder()
                .title("Carrefour")
                .description("Compra grande mensual")
                .amount(new BigDecimal("158.75"))
                .date(LocalDate.of(2026, 1, 14))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(alimentacion)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Carrefour - 158.75€");
                
            transactionRepository.save(Transaction.builder()
                .title("Panadería")
                .description("Desayunos y meriendas")
                .amount(new BigDecimal("25.60"))
                .date(LocalDate.of(2026, 1, 21))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(alimentacion)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Panadería - 25.60€");
            
            transactionRepository.save(Transaction.builder()
                .title("Gasolina")
                .description("Repostaje Repsol")
                .amount(new BigDecimal("65.40"))
                .date(LocalDate.of(2026, 1, 5))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(transporte)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Gasolina - 65.40€");
                
            transactionRepository.save(Transaction.builder()
                .title("Metro mensual")
                .description("Abono transporte público")
                .amount(new BigDecimal("54.60"))
                .date(LocalDate.of(2026, 1, 1))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(transporte)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Metro mensual - 54.60€");
            
            transactionRepository.save(Transaction.builder()
                .title("Alquiler")
                .description("Alquiler mensual apartamento")
                .amount(new BigDecimal("850.00"))
                .date(LocalDate.of(2026, 1, 1))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(vivienda)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Alquiler - 850.00€");
            
            transactionRepository.save(Transaction.builder()
                .title("Factura electricidad")
                .description("Consumo eléctrico enero")
                .amount(new BigDecimal("89.45"))
                .date(LocalDate.of(2026, 1, 15))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(facturas)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Factura electricidad - 89.45€");
                
            transactionRepository.save(Transaction.builder()
                .title("Internet + Móvil")
                .description("Factura Movistar")
                .amount(new BigDecimal("45.90"))
                .date(LocalDate.of(2026, 1, 10))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(facturas)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Internet + Móvil - 45.90€");
            
            transactionRepository.save(Transaction.builder()
                .title("Netflix")
                .description("Suscripción mensual")
                .amount(new BigDecimal("15.99"))
                .date(LocalDate.of(2026, 1, 12))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(ocio)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Netflix - 15.99€");
                
            transactionRepository.save(Transaction.builder()
                .title("Cena restaurante")
                .description("Cena con amigos")
                .amount(new BigDecimal("42.80"))
                .date(LocalDate.of(2026, 1, 18))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(ocio)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Cena restaurante - 42.80€");
                
            transactionRepository.save(Transaction.builder()
                .title("Entradas cine")
                .description("Película de estreno")
                .amount(new BigDecimal("18.00"))
                .date(LocalDate.of(2026, 1, 25))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(ocio)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Entradas cine - 18.00€");
            
            transactionRepository.save(Transaction.builder()
                .title("Zara")
                .description("Compra ropa invierno")
                .amount(new BigDecimal("89.95"))
                .date(LocalDate.of(2026, 1, 22))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(ropa)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Zara - 89.95€");

            // ========== GASTOS FEBRERO 2026 (MES ACTUAL) ==========
            
            transactionRepository.save(Transaction.builder()
                .title("Lidl")
                .description("Compra semanal")
                .amount(new BigDecimal("67.25"))
                .date(LocalDate.of(2026, 2, 3))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(alimentacion)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Lidl - 67.25€");
                
            transactionRepository.save(Transaction.builder()
                .title("Restaurante italiano")
                .description("Almuerzo de trabajo")
                .amount(new BigDecimal("35.50"))
                .date(LocalDate.of(2026, 2, 6))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(alimentacion)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Restaurante italiano - 35.50€");
            
            transactionRepository.save(Transaction.builder()
                .title("Metro mensual")
                .description("Abono febrero")
                .amount(new BigDecimal("54.60"))
                .date(LocalDate.of(2026, 2, 1))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(transporte)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Metro mensual febrero - 54.60€");
                
            transactionRepository.save(Transaction.builder()
                .title("Uber")
                .description("Viaje al aeropuerto")
                .amount(new BigDecimal("28.90"))
                .date(LocalDate.of(2026, 2, 7))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(transporte)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Uber - 28.90€");
            
            transactionRepository.save(Transaction.builder()
                .title("Alquiler")
                .description("Alquiler febrero")
                .amount(new BigDecimal("850.00"))
                .date(LocalDate.of(2026, 2, 1))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(vivienda)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Alquiler febrero - 850.00€");
            
            transactionRepository.save(Transaction.builder()
                .title("Farmacia")
                .description("Medicamentos recetados")
                .amount(new BigDecimal("23.75"))
                .date(LocalDate.of(2026, 2, 4))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(salud)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Farmacia - 23.75€");
            
            transactionRepository.save(Transaction.builder()
                .title("Curso online")
                .description("Curso de programación")
                .amount(new BigDecimal("49.99"))
                .date(LocalDate.of(2026, 2, 2))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(educacion)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Curso online - 49.99€");

            // ========== DICIEMBRE 2025 (PARA DATOS ANUALES) ==========
            
            transactionRepository.save(Transaction.builder()
                .title("Nómina Diciembre")
                .description("Sueldo diciembre + extra")
                .amount(new BigDecimal("3200.00"))
                .date(LocalDate.of(2025, 12, 31))
                .type(TransactionType.INCOME)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(nomina)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Nómina Diciembre - 3200.00€");
            
            transactionRepository.save(Transaction.builder()
                .title("Vacaciones Navidad")
                .description("Viaje familia")
                .amount(new BigDecimal("450.00"))
                .date(LocalDate.of(2025, 12, 24))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(viajes)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Vacaciones Navidad - 450.00€");
            
            transactionRepository.save(Transaction.builder()
                .title("Regalos Navidad")
                .description("Compras navideñas")
                .amount(new BigDecimal("180.65"))
                .date(LocalDate.of(2025, 12, 20))
                .type(TransactionType.EXPENSE)
                .recurrence(com.smartspend.transaction.Recurrence.NONE)
                .category(otros)
                .account(demoAccount)
                .isRecurringSeriesParent(false)
                .nextRecurrenceDate(null)
                .build());
            System.out.println("✅ Guardada: Regalos Navidad - 180.65€");

            System.out.println("🎯 RESUMEN FINAL:");
            System.out.println("📊 Total categorías: " + categoryRepository.count());
            System.out.println("💳 Total transacciones: " + transactionRepository.count());
            System.out.println("🏦 Total cuentas: " + bankAccountRepository.count());
            System.out.println("👤 Total usuarios: " + userRepository.count());
        }
    }
