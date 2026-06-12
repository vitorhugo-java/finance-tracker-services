package com.transaction.transactionservice.transaction;

import com.transaction.transactionservice.config.TestSecurityConfig;
import com.transaction.transactionservice.dto.request.CreateTransactionRequest;
import com.transaction.transactionservice.entity.TransactionType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(TestSecurityConfig.class)
public class TransactionRestAssuredTest {
    @LocalServerPort
    private int port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.4-alpine").withExposedPorts(6379);

    @Container
    static GenericContainer<?> rabbitmq = new GenericContainer<>("rabbitmq:3.13-management-alpine").withExposedPorts(5672);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);

        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void createTransaction_shouldReturnCreated() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "Test transaction",
                BigDecimal.TEN,
                "Category",
                TransactionType.INCOME,
                OffsetDateTime.now()
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/transactions")
                .then()
                .statusCode(201);
    }
}
