package com.gateway.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GatewayRoutingConfigurationTests {

    @Autowired
    private Environment environment;

    @Test
    void gatewayRoutesAreDefinedInApplicationYaml() {
        assertThat(environment.getProperty("spring.cloud.gateway.mvc.routes[0].id")).isEqualTo("transactions-route");
        assertThat(environment.getProperty("spring.cloud.gateway.mvc.routes[0].uri"))
                .isEqualTo("http://transaction-service:8080");
        assertThat(environment.getProperty("spring.cloud.gateway.mvc.routes[0].predicates[0]"))
                .isEqualTo("Path=/api/transactions/**");
        assertThat(environment.getProperty("spring.cloud.gateway.mvc.routes[0].filters[0]"))
                .isEqualTo("StripPrefix=2");

        assertThat(environment.getProperty("spring.cloud.gateway.mvc.routes[1].id")).isEqualTo("reports-route");
        assertThat(environment.getProperty("spring.cloud.gateway.mvc.routes[1].uri"))
                .isEqualTo("http://report-service:8180");
        assertThat(environment.getProperty("spring.cloud.gateway.mvc.routes[1].predicates[0]"))
                .isEqualTo("Path=/api/reports/**");
        assertThat(environment.getProperty("spring.cloud.gateway.mvc.routes[1].filters[0]"))
                .isEqualTo("StripPrefix=2");
    }
}
