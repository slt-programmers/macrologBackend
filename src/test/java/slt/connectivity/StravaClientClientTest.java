package slt.connectivity;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import slt.config.StravaConfig;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
class StravaClientClientTest {

    @Mock
    StravaConfig stravaConfig;

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    StravaClient stravaClient;

    @Test
    public void testStravaToken() {

        MockitoAnnotations.initMocks(this);

        when(stravaConfig.getClientId()).thenReturn(1);
        when(stravaConfig.getClientSecret()).thenReturn("SECRET");

        ArgumentCaptor<HttpEntity> varArgs = ArgumentCaptor.forClass(HttpEntity.class);

        ResponseEntity<StravaToken> retToken = Mockito.mock(ResponseEntity.class);
        when(retToken.getStatusCode()).thenReturn(HttpStatus.OK);
        when(retToken.getBody()).thenReturn(StravaToken.builder()
                .access_token("A")
                .refresh_token("B")
                .expires_in(1l)
                .expires_at(2l)
                .build());

        when(restTemplate.exchange(eq("https://www.strava.com/oauth/token"), eq(HttpMethod.POST), varArgs.capture(), eq(StravaToken.class)))
                .thenReturn(retToken);

        final String code = "mysecretcode";
        StravaToken stravaToken = stravaClient.getStravaToken(code);
        log.debug("token {}", stravaToken.getAccess_token());

        final HttpEntity<HashMap> value = varArgs.getValue();
        assertThat(value.getBody().get("client_id")).isEqualTo("1");
        assertThat(value.getBody().get("client_secret")).isEqualTo("SECRET");
        assertThat(value.getBody().get("code")).isEqualTo(code);
        assertThat(value.getBody().get("grant_type")).isEqualTo("authorization_code");

        assertThat(stravaToken.getAccess_token()).isEqualTo("A");
    }

    @Test
    public void testStravaTokenRestClientException() {

        MockitoAnnotations.initMocks(this);

        when(stravaConfig.getClientId()).thenReturn(1);
        when(stravaConfig.getClientSecret()).thenReturn("SECRET");

        when(restTemplate.exchange(eq("https://www.strava.com/oauth/token"), eq(HttpMethod.POST), any(), eq(StravaToken.class)))
                .thenThrow(new RestClientException("da"));

        StravaToken stravaToken = stravaClient.getStravaToken("secretCode");

        assertThat(stravaToken).isNull();
    }

    @Test
    public void testStravaTokenHttpClientException() {

        MockitoAnnotations.initMocks(this);

        when(stravaConfig.getClientId()).thenReturn(1);
        when(stravaConfig.getClientSecret()).thenReturn("SECRET");

        when(restTemplate.exchange(eq("https://www.strava.com/oauth/token"), eq(HttpMethod.POST), any(), eq(StravaToken.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "da"));

        StravaToken stravaToken = stravaClient.getStravaToken("secretCode");

        assertThat(stravaToken).isNull();
    }

    @Test
    public void refreshToken() {
        String refreshToken = "ABRACADABRA";

        MockitoAnnotations.initMocks(this);

        when(stravaConfig.getClientId()).thenReturn(1);
        when(stravaConfig.getClientSecret()).thenReturn("SECRET");

        ArgumentCaptor<HttpEntity> varArgs = ArgumentCaptor.forClass(HttpEntity.class);

        ResponseEntity<StravaToken> retToken = Mockito.mock(ResponseEntity.class);
        when(retToken.getStatusCode()).thenReturn(HttpStatus.OK);
        when(retToken.getBody()).thenReturn(StravaToken.builder()
                .access_token("A")
                .refresh_token("B")
                .expires_in(1l)
                .expires_at(2l)
                .build());

        when(restTemplate.exchange(eq("https://www.strava.com/oauth/token"), eq(HttpMethod.POST), varArgs.capture(), eq(StravaToken.class)))
                .thenReturn(retToken);

        StravaToken stravaToken = stravaClient.refreshToken(refreshToken);
        log.debug("token {}", stravaToken.getAccess_token());

        final HttpEntity<HashMap> value = varArgs.getValue();
        assertThat(value.getBody().get("client_id")).isEqualTo("1");
        assertThat(value.getBody().get("client_secret")).isEqualTo("SECRET");
        assertThat(value.getBody().get("refresh_token")).isEqualTo(refreshToken);
        assertThat(value.getBody().get("grant_type")).isEqualTo("refresh_token");

        assertThat(stravaToken.getAccess_token()).isEqualTo("A");
    }

//    @Test
//    public void getUserInfo() {
//        StravaClient stravaClient = new StravaClient();
//        stravaClient.getUserInfo();
//
//// {"id":234234,"username":"34523","resource_state":2,"firstname":"FEREW","lastname":"2342342","city":"Groningen","state":"GR","country":"The Netherlands","sex":"M","premium":false,"summit":false,"created_at":"2014-04-23T10:55:52Z","updated_at":"2019-07-20T17:47:03Z","badge_type_id":0,"profile_medium":"https://dgalywyr863hv.medium.jpg","profile":"https://ge.jpg","friend":null,"follower":null}
//
//    }

    @Test
    public void getAthleteActivities() {
        MockitoAnnotations.initMocks(this);

        ParameterizedTypeReference<List<ListedActivityDto>> parameterizedTypeReference = new ParameterizedTypeReference<List<ListedActivityDto>>() {
        };

        ResponseEntity<List<ListedActivityDto>> mockEntitity = mock(ResponseEntity.class);
        List<ListedActivityDto> retList = Arrays.asList(ListedActivityDto.builder().build());
        when(mockEntitity.getBody()).thenReturn(retList);

        ArgumentCaptor<HttpEntity> capturedHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<String> capturedUrl = ArgumentCaptor.forClass(String.class);

        when(restTemplate.exchange(capturedUrl.capture(), eq(HttpMethod.GET), capturedHttpEntity.capture(), eq(parameterizedTypeReference)))
                .thenReturn(mockEntitity);

        final List<ListedActivityDto> retActivities = stravaClient.getActivitiesForDay("myToken", LocalDate.parse("2001-01-04"));

        assertThat(retActivities).hasSize(1);

        log.debug(capturedUrl.getValue());
        final HttpHeaders headers = capturedHttpEntity.getValue().getHeaders();
        assertThat(headers.get("Authorization").get(0)).isEqualTo("Bearer myToken");

        assertThat(capturedUrl.getValue()).endsWith("?before=978652800&after=978566400"); // UTC
//        assertThat(capturedUrl.getValue()).endsWith("?before=978649200&after=978562800"); // of andersom?
    }

    @Test
    public void getAthleteActivitiesWithError() {
        MockitoAnnotations.initMocks(this);

        ParameterizedTypeReference<List<ListedActivityDto>> parameterizedTypeReference = new ParameterizedTypeReference<List<ListedActivityDto>>() {
        };

        ResponseEntity<List<ListedActivityDto>> mockEntitity = mock(ResponseEntity.class);
        List<ListedActivityDto> retList = Arrays.asList(ListedActivityDto.builder().build());
        when(mockEntitity.getBody()).thenReturn(retList);

        ArgumentCaptor<HttpEntity> capturedHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<String> capturedUrl = ArgumentCaptor.forClass(String.class);

        when(restTemplate.exchange(capturedUrl.capture(), eq(HttpMethod.GET), capturedHttpEntity.capture(), eq(parameterizedTypeReference)))
                .thenThrow(new RestClientException("oops"));

        final List<ListedActivityDto> retActivities = stravaClient.getActivitiesForDay("myToken", LocalDate.parse("2001-01-04"));

        assertThat(retActivities).isEmpty();

        log.debug(capturedUrl.getValue());
        final HttpHeaders headers = capturedHttpEntity.getValue().getHeaders();
        assertThat(headers.get("Authorization").get(0)).isEqualTo("Bearer myToken");

        assertThat(capturedUrl.getValue()).endsWith("?before=978652800&after=978566400"); // UTC
//        assertThat(capturedUrl.getValue()).endsWith("?before=978649200&after=978562800"); // of andersom?
    }

    @Test
    public void getActivityDetails() {

        String userToken = "47e01e81a381081d4f0cf1b67df25c5080ceb8b4";

        Long activityDetailId = 2558518162l;
        MockitoAnnotations.initMocks(this);

        ResponseEntity<ActivityDetailsDto> mockEntitity = mock(ResponseEntity.class);

        when(mockEntitity.getBody()).thenReturn(ActivityDetailsDto.builder().id(activityDetailId).build());

        ArgumentCaptor<HttpEntity> capturedHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<String> capturedUrl = ArgumentCaptor.forClass(String.class);

        when(restTemplate.exchange(capturedUrl.capture(), eq(HttpMethod.GET), capturedHttpEntity.capture(), eq(ActivityDetailsDto.class)))
                .thenReturn(mockEntitity);

        final ActivityDetailsDto retActivity = stravaClient.getActivityDetail(userToken, activityDetailId);

        assertThat(retActivity.getId()).isEqualTo(activityDetailId);

        final HttpHeaders headers = capturedHttpEntity.getValue().getHeaders();
        assertThat(headers.get("Authorization").get(0)).isEqualTo("Bearer " +userToken);

        assertThat(capturedUrl.getValue()).endsWith("/" + activityDetailId);

    }
//request
//    http://www.strava.com/oauth/authorize?client_id=XXXXX&response_type=code&redirect_uri=http://localhost/exchange_token&approval_prompt=force&scope=activity:read
// info about scope: https://developers.strava.com/docs/authentication/


// response
// http://localhost/exchange_token?state=&code=fqfqfqfqfqfq&scope=read
    // check scope!!

}