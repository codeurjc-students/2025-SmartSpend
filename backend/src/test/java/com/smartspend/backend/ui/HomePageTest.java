package com.smartspend.backend.ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class HomePageTest {

    static WebDriver driver;

    @BeforeAll 
    static void setup(){
        org.openqa.selenium.chrome.ChromeOptions options = new org.openqa.selenium.chrome.ChromeOptions();
        options.addArguments("--headless"); // without ui
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--user-data-dir=/tmp/chrome-profile"); 
        driver = new ChromeDriver(options);
    }
        
    @Test 
    void showTransactionsInMainPage(){

        driver.get("http://localhost:4200");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        // 1. Verificar que la página carga correctamente
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        
        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.contains("SmartspendFrontend") || pageTitle.contains("Smart"), 
                   "La página no cargó correctamente: " + pageTitle);
        
        // 2. Esperar a que aparezca el título de transacciones
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactions-title")));
        
        // 3. Verificar que hay transacciones en la lista
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("transaction-title")));
        List<WebElement> transactionItems = driver.findElements(By.className("transaction-title"));
        
        assertTrue(transactionItems.size() > 0, "No se encontraron transacciones en la página");
        
        // 4. Verificar que al menos una transacción tiene contenido
        boolean hasTransactionWithText = transactionItems.stream()
            .anyMatch(item -> !item.getText().trim().isEmpty());
        assertTrue(hasTransactionWithText, "Las transacciones no tienen contenido");
        
        System.out.println("✅ Test exitoso: Página carga y muestra " + transactionItems.size() + " transacciones");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }



}
