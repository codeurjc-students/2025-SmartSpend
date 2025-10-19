package com.smartspend.backend.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("ssl")
public class TransactionApiTest {

	@LocalServerPort
	private int port;

	@BeforeEach
	void setup() {
		RestAssured.baseURI = "https://localhost";
		RestAssured.port = port;	
		RestAssured.useRelaxedHTTPSValidation();
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
