package slt.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HealthcheckControllerTest {

    private HealthcheckController controller;

    @BeforeEach
    void setup() {
        controller = new HealthcheckController();
    }

    @Test
    void isHealthy() {
        final var result = controller.isHealthy();
        Assertions.assertEquals(Boolean.TRUE, result.getBody());
    }
}
