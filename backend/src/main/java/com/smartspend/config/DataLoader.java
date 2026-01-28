package com.smartspend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.smartspend.category.Category;
import com.smartspend.category.CategoryRepository;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;


    @Override
    public void run(String... args) throws Exception {
        // Precargar categorías solo si no hay ninguna categoría existente
        if (categoryRepository.count() == 0) {
            System.out.println("Cargando categorías iniciales por defecto...");

            userRepository.save(new User("admin", "admin@gmail.com", "admin123"));



            // Categorías de INGRESO
            categoryRepository.save(new Category("Nómina", "Ingresos por trabajo", "#27ae60", TransactionType.INCOME, "💰"));
            categoryRepository.save(new Category("Venta", "Venta de productos/servicios", "#2ecc71", TransactionType.INCOME, "💸"));
            categoryRepository.save(new Category("Regalo", "Regalos o donaciones", "#3498db", TransactionType.INCOME, "🎁"));
            categoryRepository.save(new Category("Inversión", "Beneficios de inversiones", "#1abc9c", TransactionType.INCOME, "📈"));

            // Categorías de GASTO
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
            System.out.println("Ya existen categorías, omitiendo la carga inicial de categorías.");
        }
    }
}