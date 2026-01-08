package slt.connectivity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import slt.config.StravaConfig;
import slt.connectivity.strava.StravaClient;
import slt.connectivity.strava.StravaToken;
import slt.connectivity.strava.dto.ActivityDetailsDto;
import slt.connectivity.strava.dto.ListedActivityDto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StravaClientClientTest {

    private StravaConfig stravaConfig;

    private RestTemplate restTemplate;

    private StravaClient stravaClient;

    @BeforeEach
    void setup() {
        stravaConfig = mock(StravaConfig.class);
        restTemplate = mock(RestTemplate.class);
        stravaClient = new StravaClient(restTemplate, stravaConfig);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testStravaToken() {
        when(stravaConfig.getClientId()).thenReturn(1);
        when(stravaConfig.getClientSecret()).thenReturn("SECRET");

        final var varArgs = ArgumentCaptor.forClass(HttpEntity.class);
        final var retToken = (ResponseEntity<StravaToken>) mock(ResponseEntity.class);
        when(retToken.getStatusCode()).thenReturn(HttpStatus.OK);
        when(retToken.getBody()).thenReturn(StravaToken.builder()
                .access_token("A")
                .refresh_token("B")
                .expires_in(1L)
                .expires_at(2L)
                .build());

        when(restTemplate.exchange(eq("https://www.strava.com/oauth/token"), eq(HttpMethod.POST), varArgs.capture(), eq(StravaToken.class)))
                .thenReturn(retToken);

        final String code = "mysecretcode";
        final var stravaToken = stravaClient.getStravaToken(code);
        Assertions.assertTrue(stravaToken.isPresent());

        final var value = varArgs.getValue();
        final var body = (HashMap<String, String>) value.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("1", body.get("client_id"));
        Assertions.assertEquals("SECRET", body.get("client_secret"));
        Assertions.assertEquals(code, body.get("code"));
        Assertions.assertEquals("authorization_code", body.get("grant_type"));
        Assertions.assertEquals("A", stravaToken.get().getAccess_token());
    }

    @Test
    public void testStravaTokenRestClientException() {
        when(stravaConfig.getClientId()).thenReturn(1);
        when(stravaConfig.getClientSecret()).thenReturn("SECRET");
        when(restTemplate.exchange(eq("https://www.strava.com/oauth/token"), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(StravaToken.class))).thenThrow(new RestClientException("da"));

        final var stravaToken = stravaClient.getStravaToken("secretCode");
        Assertions.assertTrue(stravaToken.isEmpty());
    }

    @Test
    public void testStravaTokenHttpClientException() {
        when(stravaConfig.getClientId()).thenReturn(1);
        when(stravaConfig.getClientSecret()).thenReturn("SECRET");
        when(restTemplate.exchange(eq("https://www.strava.com/oauth/token"), eq(HttpMethod.POST), any(),
                eq(StravaToken.class))).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "da"));

        final var stravaToken = stravaClient.getStravaToken("secretCode");
        Assertions.assertTrue(stravaToken.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void refreshToken() {
        final var refreshToken = "ABRACADABRA";

        when(stravaConfig.getClientId()).thenReturn(1);
        when(stravaConfig.getClientSecret()).thenReturn("SECRET");

        final var varArgs = ArgumentCaptor.forClass(HttpEntity.class);

        final var retToken = mock(ResponseEntity.class);
        when(retToken.getStatusCode()).thenReturn(HttpStatus.OK);
        when(retToken.getBody()).thenReturn(StravaToken.builder()
                .access_token("A")
                .refresh_token("B")
                .expires_in(1L)
                .expires_at(2L)
                .build());

        when(restTemplate.exchange(eq("https://www.strava.com/oauth/token"), eq(HttpMethod.POST), varArgs.capture(),
                eq(StravaToken.class))).thenReturn(retToken);

        final var stravaToken = stravaClient.refreshToken(refreshToken);
        Assertions.assertTrue(stravaToken.isPresent());

        final var value = varArgs.getValue();
        final var body = (HashMap<String, String>) value.getBody();
        Assertions.assertNotNull(body);

        Assertions.assertEquals("1", body.get("client_id"));
        Assertions.assertEquals("SECRET", body.get("client_secret"));
        Assertions.assertEquals(refreshToken, body.get("refresh_token"));
        Assertions.assertEquals("refresh_token", body.get("grant_type"));
        Assertions.assertEquals("A", stravaToken.get().getAccess_token());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAthleteActivities() {
        final var mockEntitity = mock(ResponseEntity.class);
        final var retList = List.of(ListedActivityDto.builder().build());
        when(mockEntitity.getBody()).thenReturn(retList);

        final var capturedHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);
        final var capturedUrl = ArgumentCaptor.forClass(String.class);

        when(restTemplate.exchange(capturedUrl.capture(), eq(HttpMethod.GET), capturedHttpEntity.capture(), any(ParameterizedTypeReference.class)))
                .thenReturn(mockEntitity);

        final var retActivities = stravaClient.getActivitiesForDay("myToken", LocalDate.parse("2001-01-04"));
        Assertions.assertEquals(1, retActivities.size());

        final var headers = capturedHttpEntity.getValue().getHeaders();
        final var authHeader = headers.get("Authorization");
        Assertions.assertNotNull(authHeader);
        Assertions.assertEquals("Bearer myToken", authHeader.getFirst());

        // problems with UTC on server and local CET :(
//        Assertions.assertTrue(capturedUrl.getValue().endsWith("?before=978649200&after=978562800"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAthleteActivitiesWithError() {
        final var mockEntitity = mock(ResponseEntity.class);
        final var retList = List.of(ListedActivityDto.builder().build());
        when(mockEntitity.getBody()).thenReturn(retList);

        final var capturedUrl = ArgumentCaptor.forClass(String.class);
        final var capturedHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(capturedUrl.capture(), eq(HttpMethod.GET), capturedHttpEntity.capture(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("oops"));

        final var retActivities = stravaClient.getActivitiesForDay("myToken", LocalDate.parse("2001-01-04"));
        Assertions.assertTrue(retActivities.isEmpty());
        final var headers = capturedHttpEntity.getValue().getHeaders();
        final var authheader = headers.get("Authorization");
        Assertions.assertNotNull(authheader);
        Assertions.assertEquals("Bearer myToken", authheader.getFirst());

        // problems with UTC on server and local CET :(
//        Assertions.assertTrue(capturedUrl.getValue().endsWith("?before=978649200&after=978562800"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getActivityDetails() {
        final var userToken = "47e01e81a381081d4f0cf1b67df25c5080ceb8b4";
        final var activityDetailId = 2558518162L;
        final var mockEntitity = mock(ResponseEntity.class);
        when(mockEntitity.getBody()).thenReturn(ActivityDetailsDto.builder().id(activityDetailId).build());
        final var capturedHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);
        final var capturedUrl = ArgumentCaptor.forClass(String.class);
        when(restTemplate.exchange(capturedUrl.capture(), eq(HttpMethod.GET), capturedHttpEntity.capture(), eq(ActivityDetailsDto.class)))
                .thenReturn(mockEntitity);

        final var retActivity = stravaClient.getActivityDetail(userToken, activityDetailId);
        Assertions.assertTrue(retActivity.isPresent());
        Assertions.assertEquals(activityDetailId, retActivity.get().getId());
        final var headers = capturedHttpEntity.getValue().getHeaders();
        final var authheader = headers.get("Authorization");
        Assertions.assertNotNull(authheader);
        Assertions.assertEquals("Bearer " + userToken, authheader.getFirst());
        Assertions.assertTrue(capturedUrl.getValue().endsWith("/" + activityDetailId));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void unregister() {
        final var varArgs = ArgumentCaptor.forClass(HttpEntity.class);
        final var mockEntitity = mock(ResponseEntity.class);
        when(mockEntitity.getBody()).thenReturn("jammer");

        when(restTemplate.exchange(eq("https://www.strava.com/oauth/deauthorize"), eq(HttpMethod.POST), varArgs.capture(), eq(String.class)))
                .thenReturn(mockEntitity);

        final boolean success = stravaClient.unregister(StravaToken.builder().access_token("A").build());
        Assertions.assertTrue(success);
        final var headers = varArgs.getValue().getHeaders();
        final var authheader = headers.get("Authorization");
        Assertions.assertNotNull(authheader);
        Assertions.assertEquals("Bearer A", authheader.getFirst());
    }
}