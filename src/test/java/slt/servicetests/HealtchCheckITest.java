package slt.servicetests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import slt.rest.HealthcheckService;
import slt.servicetests.utils.AbstractApplicationIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class HealtchCheckITest  extends AbstractApplicationIntegrationTest {

    @Autowired
    HealthcheckService healthcheckService;

    @Test
    public void testIsHealth() {
        ResponseEntity healthy = healthcheckService.isHealthy();
        assertThat(healthy.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }
}
