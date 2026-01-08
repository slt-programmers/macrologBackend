package slt.connectivity.google;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import slt.config.GoogleConfig;
import slt.connectivity.google.dto.Oath2Token;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GoogleClientTest {

    private GoogleConfig googleConfig;
    private RestTemplate restTemplate;
    private GoogleClient googleClient;

    @BeforeEach
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        googleConfig = mock(GoogleConfig.class);
        googleClient = new GoogleClient(restTemplate, googleConfig);
    }

    @Test
    @SuppressWarnings("unchecked")
    void refreshToken() {
        when(googleConfig.getClientId()).thenReturn("1");
        when(googleConfig.getClientSecret()).thenReturn("2");

        final var response = mock(ResponseEntity.class);
        when(response.getBody()).thenReturn(Oath2Token.builder().build());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Oath2Token.class))).thenReturn(response);

        final var refreshedToken = googleClient.refreshToken("r");
        Assertions.assertTrue(refreshedToken.isPresent());
    }

    @Test
    void refreshTokenClientError() {
        when(googleConfig.getClientId()).thenReturn("1");
        when(googleConfig.getClientSecret()).thenReturn("2");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Oath2Token.class))).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        final var refreshedToken = googleClient.refreshToken("r");
        Assertions.assertTrue(refreshedToken.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void refreshTokenRestError() {
        when(googleConfig.getClientId()).thenReturn("1");
        when(googleConfig.getClientSecret()).thenReturn("2");

        final var argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), argumentCaptor.capture(),
                eq(Oath2Token.class))).thenThrow(new RestClientException("a"));

        final var refreshedToken = googleClient.refreshToken("r");
        Assertions.assertTrue(refreshedToken.isEmpty());
        final var body = (HashMap<String, String>) argumentCaptor.getValue().getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("refresh_token", body.get("grant_type"));
        Assertions.assertEquals("r", body.get("refresh_token"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAuthorizationToken() {
        when(googleConfig.getClientId()).thenReturn("1");
        when(googleConfig.getClientSecret()).thenReturn("2");

        final var argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), argumentCaptor.capture(), eq(Oath2Token.class))).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        final var refreshedToken = googleClient.getAuthorizationToken("r");
        Assertions.assertTrue(refreshedToken.isEmpty());
        final var body = (HashMap<String, String>) argumentCaptor.getValue().getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("authorization_code", body.get("grant_type"));
        Assertions.assertEquals("r", body.get("code"));
    }

    @Test
    void sendMailNoToken() throws IOException, GeneralSecurityException {
        final var message = mock(Message.class);
        googleClient.sendMail(null, message);
        verify(googleConfig, times(0)).getApplicationName();
    }

    @Test
    void sendMailWithToken() {
        final var message = mock(Message.class);
        Assertions.assertThrows(GoogleJsonResponseException.class, () -> googleClient.sendMail(Oath2Token.builder().build(), message));
    }

    @Test
    void createMail() throws MessagingException {
        final MimeMessage email = googleClient.createEmail("to", "from", "subject", "body");
        Assertions.assertEquals(1, email.getFrom().length);
        Assertions.assertEquals(1, email.getAllRecipients().length);
    }

    @Test
    void createGoogleMail() throws IOException, MessagingException {
        final var message = mock(MimeMessage.class);
        final com.google.api.services.gmail.model.Message messageWithEmail = googleClient.createMessageWithEmail(message);
        Assertions.assertNotNull(messageWithEmail.getRaw());
    }
}