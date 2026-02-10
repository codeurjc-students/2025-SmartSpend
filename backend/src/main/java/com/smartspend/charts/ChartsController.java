package com.smartspend.charts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.smartspend.charts.dtos.PieChartDto;
import com.smartspend.transaction.TransactionType;

@RestController 
@RequestMapping("/api/v1/charts")
public class ChartsController {
    
    @Autowired
    private ChartsService chartsService;
    
    /**
     * Obtiene estadísticas de categorías por mes para un gráfico de pastel
     * @param accountId ID de la cuenta bancaria
     * @param year Año (ejemplo: 2025)
     * @param month Mes (1-12)
     * @param type Tipo de transacción (INCOME o EXPENSE)
     * @param authentication Usuario autenticado
     * @return Datos para el gráfico de pastel
     */
    @GetMapping("/pie/monthly")
    public ResponseEntity<PieChartDto> getPieChartByMonth(
            @RequestParam Long accountId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam TransactionType type,
            Authentication authentication) {
        
        System.out.println("Received request for monthly pie chart with accountId: " + accountId + ", year: " + year + ", month: " + month + ", type: " + type);
        try {
            String userEmail = authentication.getName();
            PieChartDto pieChartData = chartsService.getCategoryStadsByMonth(
                userEmail, 
                accountId, 
                year, 
                month, 
                type
            );
            return ResponseEntity.ok(pieChartData);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtiene estadísticas de categorías por año para un gráfico de pastel
     * @param accountId ID de la cuenta bancaria
     * @param year Año (ejemplo: 2025)
     * @param type Tipo de transacción (INCOME o EXPENSE)
     * @param authentication Usuario autenticado
     * @return Datos para el gráfico de pastel
     */
    @GetMapping("/pie/yearly")
    public ResponseEntity<PieChartDto> getPieChartByYear(
            @RequestParam Long accountId,
            @RequestParam int year,
            @RequestParam TransactionType type,
            Authentication authentication) {
        
        System.out.println("Received request for yearly pie chart with accountId: " + accountId + ", year: " + year + ", type: " + type);
        try {
            String userEmail = authentication.getName();
            PieChartDto pieChartData = chartsService.getCategoryStadsByYear(
                userEmail, 
                accountId, 
                year, 
                type
            );
            return ResponseEntity.ok(pieChartData);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
