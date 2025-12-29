package slt.integrationtests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import slt.rest.HealthcheckController;
import slt.integrationtests.utils.AbstractApplicationIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class HealthCheckITest  extends AbstractApplicationIntegrationTest {

    @Autowired
    HealthcheckController healthcheckController;

    @Test
    public void testIsHealth() {
        final var healthy = healthcheckController.isHealthy();
        assertThat(healthy.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
