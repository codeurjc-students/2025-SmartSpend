package com.smartspend.backend.ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class HomePageTest {

    static WebDriver driver;

    @BeforeAll 
    static void setup(){
        driver = new ChromeDriver();
    }

    @Test 
    void showTransactionsInMainPage(){

        driver.get("http://localhost:4200");

        List<WebElement> items = driver.findElements(By.className("transaction-title"));
        assertTrue(items.size() > 0, "No se encontraron transacciones");
        
        boolean found = items.stream()
            .anyMatch(item -> item.getText().contains("Nómina Septiembre"));
        assertTrue(found, "No se encontró la transacción 'Nómina Septiembre'");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }



}
