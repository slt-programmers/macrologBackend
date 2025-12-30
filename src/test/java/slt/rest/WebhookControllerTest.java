package slt.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import slt.config.StravaConfig;
import slt.connectivity.strava.StravaActivityService;
import slt.connectivity.strava.dto.SubscriptionInformation;
import slt.connectivity.strava.dto.WebhookEvent;
import slt.service.AdminService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static slt.rest.WebhookController.NOT_AUTHORIZED_TO_ALTER_WEBHOOKS_MESSAGE;

class WebhookControllerTest {

    private StravaConfig stravaConfig;
    private StravaActivityService stravaActivityService;
    private AdminService adminService;
    private WebhookController webhookController;

    @BeforeEach
    void setUp() {
        stravaConfig = mock(StravaConfig.class);
        stravaActivityService = mock(StravaActivityService.class);
        adminService = mock(AdminService.class);
        webhookController = new WebhookController(stravaActivityService, adminService, stravaConfig);
    }

    @Test
    void syncStrava() {
        final var responseEntity = webhookController.syncStrava(WebhookEvent.builder().build());
        verify(stravaActivityService).receiveWebhookEvent(isA(WebhookEvent.class));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verifyNoMoreInteractions(stravaActivityService, stravaConfig);
    }

    @Test
    void syncStravaCallbackWrongHubmode() {
        final var responseEntity = webhookController.syncStravaCallback("hubmode", "challenge", "token");
        verifyNoMoreInteractions(stravaActivityService, stravaConfig);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void syncStravaCallbackWrongVerifyToken() {
        when(stravaConfig.getVerifytoken()).thenReturn("token");
        final var responseEntity = webhookController.syncStravaCallback("subscribe", "challenge", "verkeerd");
        verify(stravaConfig).getVerifytoken();
        verifyNoMoreInteractions(stravaActivityService, stravaConfig);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void syncStravaCallback() {
        when(stravaConfig.getVerifytoken()).thenReturn("token");
        final var responseEntity = webhookController.syncStravaCallback("subscribe", "challenge", "token");
        verify(stravaConfig).getVerifytoken();
        verifyNoMoreInteractions(stravaActivityService, stravaConfig);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo("{\"hub.challenge\":\"challenge\"}");
    }

    @Test
    void startWebhook() {
        final var subscriptionInformation = SubscriptionInformation.builder().id(1).callback_url("callback").build();
        when(stravaActivityService.startWebhookSubcription()).thenReturn(subscriptionInformation);
        final var responseEntity = webhookController.startWebhook();
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        final var response = responseEntity.getBody();
        Assertions.assertEquals(1, response.getId());
        Assertions.assertEquals("callback", response.getCallback_url());
        verify(stravaActivityService).startWebhookSubcription();
        verify(adminService).verifyAdmin(NOT_AUTHORIZED_TO_ALTER_WEBHOOKS_MESSAGE);
    }

    @Test
    void endWebhookAdmin() {
        final var responseEntity = webhookController.endWebhook(2);
        verify(stravaActivityService).endWebhookSubscription(eq(2));
        verifyNoMoreInteractions(stravaActivityService, stravaConfig);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(adminService).verifyAdmin(NOT_AUTHORIZED_TO_ALTER_WEBHOOKS_MESSAGE);
    }

    @Test
    void getWebhookAdmin() {
        final var subscriptionInformation = SubscriptionInformation.builder().id(1).callback_url("callback").build();
        when(stravaActivityService.getWebhookSubscription()).thenReturn(subscriptionInformation);
        final var responseEntity = webhookController.getWebhook();
        verify(stravaActivityService).getWebhookSubscription();
        verifyNoMoreInteractions(stravaActivityService, stravaConfig);
        verify(adminService).verifyAdmin(NOT_AUTHORIZED_TO_ALTER_WEBHOOKS_MESSAGE);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        final var response = responseEntity.getBody();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.getId());
        Assertions.assertEquals("callback", response.getCallback_url());    }

}