package com.smartspend.system.config;

import io.restassured.RestAssured;
import io.restassured.config.SSLConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for API system tests with SSL configuration
 * Provides common setup for Rest Assured with SSL trust store
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "server.ssl.enabled=true",
    "server.ssl.key-store-type=PKCS12",
    "server.ssl.key-store=classpath:keystore/keystore.p12",
    "server.ssl.key-store-password=password",
    "server.ssl.key-alias=smartspend-ssl",
    "server.ssl.trust-store=classpath:keystore/truststore.p12",
    "server.ssl.trust-store-password=password"
})
public abstract class BaseApiTest {

    @LocalServerPort
    protected int port;

    protected String baseUri;

    @BeforeEach
    void setupRestAssured() {
        baseUri = "https://localhost:" + port;
        
        RestAssured.baseURI = "https://localhost";
        RestAssured.port = port;
        RestAssured.useRelaxedHTTPSValidation(); // For testing with self-signed certificates
        
        // Alternative: Configure with trust store
        // RestAssured.config = RestAssuredConfig.config()
        //     .sslConfig(SSLConfig.sslConfig()
        //         .trustStore("src/test/resources/keystore/truststore.p12", "password"));
    }

    protected String getApiBaseUrl() {
        return baseUri + "/api/v1";
    }

    protected String getAuthToken() {
        // Override in subclasses to provide authentication token
        return null;
    }
}