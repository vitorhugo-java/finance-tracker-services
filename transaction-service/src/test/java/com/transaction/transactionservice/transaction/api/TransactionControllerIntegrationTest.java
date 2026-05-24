package com.transaction.transactionservice.transaction.api;

import com.transaction.transactionservice.support.IntegrationTestContainers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerIntegrationTest extends IntegrationTestContainers {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createsAndReplaysTransactionWithIdempotencyKey() throws Exception {
        UUID userId = UUID.randomUUID();
        String body = """
                {"description":"Salary payment","amount":2500.00,"type":"INCOME","category":"Salary","transactionDate":"2026-05-24"}
                """;

        mockMvc.perform(post("/transactions")
                        .header("X-User-Id", userId)
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "USER")
                        .header("Idempotency-Key", "same-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.userId", is(userId.toString())));

        mockMvc.perform(post("/transactions")
                        .header("X-User-Id", userId)
                        .header("Idempotency-Key", "same-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        mockMvc.perform(get("/transactions")
                        .header("X-User-Id", userId)
                        .param("category", "Salary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void returnsValidationErrors() throws Exception {
        mockMvc.perform(post("/transactions")
                        .header("X-User-Id", UUID.randomUUID())
                        .header("Idempotency-Key", "validation-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"","amount":0,"type":"EXPENSE","category":"","transactionDate":"2026-05-24"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields", hasSize(3)));
    }
}
