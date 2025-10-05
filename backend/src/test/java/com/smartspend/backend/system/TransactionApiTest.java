package com.smartspend.backend.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionApiTest {
    @BeforeAll 
	static void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 8080;	
	}

	@Test 
	void getTransactions(){
		given()
		.when().get("api/v1/transactions")
		.then()
		.statusCode(200)
		.body("title", hasItems(
            "Nómina Septiembre", 
            "Suscripción Netflix"
        ));
	}
}
