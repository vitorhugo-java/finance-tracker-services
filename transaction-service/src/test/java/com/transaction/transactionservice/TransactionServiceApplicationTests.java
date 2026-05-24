package com.transaction.transactionservice;

import com.transaction.transactionservice.support.IntegrationTestContainers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TransactionServiceApplicationTests extends IntegrationTestContainers {

    @Test
    void contextLoads() {
    }

}
