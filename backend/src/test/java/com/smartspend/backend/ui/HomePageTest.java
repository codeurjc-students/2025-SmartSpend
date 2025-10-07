package com.smartspend.backend.ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

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

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
