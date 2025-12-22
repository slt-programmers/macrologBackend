package slt.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import slt.config.StravaConfig;
import slt.connectivity.strava.StravaActivityService;
import slt.connectivity.strava.dto.SubscriptionInformation;
import slt.connectivity.strava.dto.WebhookEvent;
import slt.database.UserAccountRepository;
import slt.database.entities.UserAccount;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

class WebhookControllerTest {

    @Mock
    StravaConfig stravaConfig;

    @Mock
    StravaActivityService stravaActivityService;

    @Mock
    UserAccountRepository userAccountRepository;

    @InjectMocks
    WebhookController webhookController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void syncStrava() {
        final var responseEntity = webhookController.syncStrava(WebhookEvent.builder().build());
        verify(stravaActivityService).receiveWebHookEvent(isA(WebhookEvent.class));
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
    void startWebhookNotAdmin() {
        ThreadLocalHolder.getThreadLocal().set(new UserInfo());
        when(userAccountRepository.getUserById(any())).thenReturn(UserAccount.builder().id(12L).build());
        final var responseEntity = webhookController.startWebhook();

        verify(userAccountRepository).getUserById(any());
        verifyNoMoreInteractions(stravaActivityService, stravaConfig, userAccountRepository);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void startWebhookAdmin() {
        ThreadLocalHolder.getThreadLocal().set(new UserInfo());
        when(userAccountRepository.getUserById(any())).thenReturn(UserAccount.builder().id(12L).isAdmin(true).build());
        final var responseEntity = webhookController.startWebhook();

        verify(userAccountRepository).getUserById(any());
        verify(stravaActivityService).startWebhookSubcription();
        verifyNoMoreInteractions(stravaActivityService, stravaConfig, userAccountRepository);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    void endWebhookNotAdmin() {
        ThreadLocalHolder.getThreadLocal().set(new UserInfo());
        when(userAccountRepository.getUserById(any())).thenReturn(UserAccount.builder().id(12L).build());
        final var responseEntity = webhookController.endWebhook(2);

        verify(userAccountRepository).getUserById(any());
        verifyNoMoreInteractions(stravaActivityService, stravaConfig, userAccountRepository);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void endWebhookAdmin() {
        ThreadLocalHolder.getThreadLocal().set(new UserInfo());
        when(userAccountRepository.getUserById(any())).thenReturn(UserAccount.builder().id(12L).isAdmin(true).build());
        final var responseEntity = webhookController.endWebhook(2);

        verify(userAccountRepository).getUserById(any());
        verify(stravaActivityService).endWebhookSubscription(eq(2));
        verifyNoMoreInteractions(stravaActivityService, stravaConfig, userAccountRepository);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    void getWebhookNotAdmin() {
        ThreadLocalHolder.getThreadLocal().set(new UserInfo());
        when(userAccountRepository.getUserById(any())).thenReturn(UserAccount.builder().id(12L).build());
        final var responseEntity = webhookController.getWebhook();

        verify(userAccountRepository).getUserById(any());
        verifyNoMoreInteractions(stravaActivityService, stravaConfig, userAccountRepository);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getWebhookAdmin() {
        ThreadLocalHolder.getThreadLocal().set(new UserInfo());
        when(userAccountRepository.getUserById(any())).thenReturn(UserAccount.builder().id(12L).isAdmin(true).build());
        when(stravaActivityService.getWebhookSubscription()).thenReturn(SubscriptionInformation.builder().build());
        final var responseEntity = webhookController.getWebhook();

        verify(userAccountRepository).getUserById(any());
        verify(stravaActivityService).getWebhookSubscription();
        verifyNoMoreInteractions(stravaActivityService, stravaConfig, userAccountRepository);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isInstanceOf(SubscriptionInformation.class);
    }

}