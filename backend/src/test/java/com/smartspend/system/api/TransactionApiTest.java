// package com.smartspend.system.api;

// import com.smartspend.system.config.BaseApiTest;
// import org.junit.jupiter.api.Test;
// import static io.restassured.RestAssured.given;
// import static org.hamcrest.Matchers.hasItems;

// /**
//  * System tests for Transaction API endpoints
//  * Tests the complete API flow with SSL enabled
//  */
// public class TransactionApiTest extends BaseApiTest {

// 	@Test 
// 	void shouldGetTransactionsWithSSL(){
// 		given()
// 		.when().get(getApiBaseUrl() + "/transactions")
// 		.then()
// 		.statusCode(200)
// 		.body("title", hasItems(
//             "Nómina Septiembre", 
//             "Suscripción Netflix"
//         ));
// 	}
// }
