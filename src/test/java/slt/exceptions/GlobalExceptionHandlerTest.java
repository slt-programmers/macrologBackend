package slt.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

    final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleNotFound() {
        final var ex = new NotFoundException("Nothing here");
        final var result = globalExceptionHandler.handle(ex);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals("Nothing here", result.getBody().message());
    }

    @Test
    void testHandleInvalidDate() {
        final var ex = new InvalidDateException("Not today");
        final var result = globalExceptionHandler.handle(ex);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals("Not today", result.getBody().message());
    }

    @Test
    void testHandleValidationException() {
        final var ex = new ValidationException("Not like this");
        final var result = globalExceptionHandler.handle(ex);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals("Not like this", result.getBody().message());
    }

    @Test
    void testHandleUnathorized() {
        final var ex = new UnauthorizedException("Not you");
        final var result = globalExceptionHandler.handle(ex);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals("Not you", result.getBody().message());
    }

    @Test
    void testHandleWebhookSetup() {
        final var ex = new ConnectivityException("Not them");
        final var result = globalExceptionHandler.handle(ex);
        Assertions.assertEquals(HttpStatus.FAILED_DEPENDENCY, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals("Not them", result.getBody().message());
    }
}