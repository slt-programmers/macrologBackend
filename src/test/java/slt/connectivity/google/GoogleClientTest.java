package slt.connectivity.google;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import slt.config.GoogleConfig;
import slt.connectivity.oath2.Oath2Token;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class GoogleClientTest {

    @Mock
    GoogleConfig googleConfig;

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    GoogleClient googleClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void sendMail() {
    }

    @Test
    void createEmail() {
    }

    @Test
    void createMessageWithEmail() {
    }


    @Test
    void refreshToken() {
        when(googleConfig.getClientId()).thenReturn("1");
        when(googleConfig.getClientSecret()).thenReturn("2");

        ResponseEntity<Oath2Token> response = mock(ResponseEntity.class);
        when(response.getBody()).thenReturn(Oath2Token.builder().build());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Oath2Token.class))).thenReturn(response);

        final Oath2Token refreshedToken = googleClient.refreshToken("r");
        Assertions.assertThat(refreshedToken).isNotNull();
    }

    @Test
    void refreshTokenClientError() {
        when(googleConfig.getClientId()).thenReturn("1");
        when(googleConfig.getClientSecret()).thenReturn("2");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Oath2Token.class))).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        final Oath2Token refreshedToken = googleClient.refreshToken("r");
        Assertions.assertThat(refreshedToken).isNull();
    }

    @Test
    void refreshTokenRestError() {
        when(googleConfig.getClientId()).thenReturn("1");
        when(googleConfig.getClientSecret()).thenReturn("2");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Oath2Token.class))).thenThrow(new RestClientException("a"));

        final Oath2Token refreshedToken = googleClient.refreshToken("r");
        Assertions.assertThat(refreshedToken).isNull();
    }
}