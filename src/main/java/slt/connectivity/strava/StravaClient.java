package slt.connectivity.strava;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import slt.config.StravaConfig;
import slt.connectivity.strava.dto.ActivityDetailsDto;
import slt.connectivity.strava.dto.ListedActivityDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * See https://developers.strava.com/docs/authentication/ for implementation used.
 */
@Slf4j
@Component
@AllArgsConstructor
@NoArgsConstructor
public class StravaClient {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    StravaConfig stravaConfig;

    private static final String ERROR_MESSAGE = "Fout bij versturen.";
    private static final String BEARER_MESSAGE = "Bearer %s";


    public StravaToken getStravaToken(String authorizationCode) {
        String grantType = "authorization_code";
        String tokenUrl = "https://www.strava.com/oauth/token";

        String clientId = stravaConfig.getClientId().toString();
        String clientSecret = stravaConfig.getClientSecret();
        Map reqPayload = new HashMap();
        reqPayload.put("client_id", clientId);
        reqPayload.put("client_secret", clientSecret);
        reqPayload.put("code", authorizationCode);
        reqPayload.put("grant_type", grantType);

        return getStravaToken(tokenUrl, reqPayload);

    }

    private StravaToken getStravaToken(String tokenUrl, Map reqPayload) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            final HttpEntity<HashMap> entity = new HttpEntity(reqPayload, headers);
            ResponseEntity<StravaToken> responseEntity = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, StravaToken.class);

            StravaToken gevondenToken = responseEntity.getBody();
            log.debug("Gevonden token {}", gevondenToken.access_token);
            return gevondenToken;

        } catch (HttpClientErrorException httpClientErrorException) {
            log.error(httpClientErrorException.getResponseBodyAsString());
            log.error(ERROR_MESSAGE + " {}", httpClientErrorException.getLocalizedMessage(), httpClientErrorException);
            return null;
        } catch (RestClientException restClientException) {
            log.error(ERROR_MESSAGE + " {}", restClientException.getLocalizedMessage(), restClientException);
            return null;
        }
    }

    public StravaToken refreshToken(String refreshToken) {
        String tokenUrl = "https://www.strava.com/oauth/token";
        String grantType = "refresh_token";

        String clientId = stravaConfig.getClientId().toString();
        String clientSecret = stravaConfig.getClientSecret();

        Map reqPayload = new HashMap();
        reqPayload.put("client_id", clientId);
        reqPayload.put("client_secret", clientSecret);
        reqPayload.put("refresh_token", refreshToken);
        reqPayload.put("grant_type", grantType);

        return getStravaToken(tokenUrl, reqPayload);
    }

    private long getUTCEpoch(LocalDateTime localDateTime) {
        ZonedDateTime ldtZoned = localDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
        return utcZoned.toEpochSecond();
    }

    public List<ListedActivityDto> getActivitiesForDay(String token, LocalDate date) {
        try {
            String url = "https://www.strava.com/api/v3/athlete/activities";

            final LocalDateTime localDateTimeStartOfDay = date.atStartOfDay();
            final LocalDateTime localDateTimeEndOfDay = date.atStartOfDay().plusDays(1);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("before", getUTCEpoch(localDateTimeEndOfDay))
                    .queryParam("after", getUTCEpoch(localDateTimeStartOfDay));

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(HttpHeaders.AUTHORIZATION, String.format(BEARER_MESSAGE, token));

            final HttpEntity entity = new HttpEntity<>(headers);
            ParameterizedTypeReference<List<ListedActivityDto>> parameterizedTypeReference = new ParameterizedTypeReference<List<ListedActivityDto>>() {
            };
            ResponseEntity<List<ListedActivityDto>> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, parameterizedTypeReference);

            return responseEntity.getBody();
        } catch (HttpClientErrorException httpClientErrorException) {
            log.error(httpClientErrorException.getResponseBodyAsString());
            log.error(ERROR_MESSAGE + " {}", httpClientErrorException.getLocalizedMessage(), httpClientErrorException);
            return new ArrayList<>();
        } catch (RestClientException restClientException) {

            log.error(ERROR_MESSAGE + " {}", restClientException.getLocalizedMessage(), restClientException);
            return new ArrayList<>();
        }
    }

    public ActivityDetailsDto getActivityDetail(String token, Long activityDetailId) {
        try {
            String url = "https://www.strava.com/api/v3/activities";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + "/" + activityDetailId);

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(HttpHeaders.AUTHORIZATION, String.format(BEARER_MESSAGE, token));

            final HttpEntity<ActivityDetailsDto> entity = new HttpEntity<>(headers);
            ResponseEntity<ActivityDetailsDto> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, ActivityDetailsDto.class);

            ActivityDetailsDto gevondenActivity = responseEntity.getBody();

            log.debug(gevondenActivity.getStart_date_local() + " - " + gevondenActivity.getCalories() + " - " + gevondenActivity.getName() + " " + gevondenActivity.getType() + " " + gevondenActivity.getId());
            return gevondenActivity;

        } catch (HttpClientErrorException httpClientErrorException) {
            log.error(httpClientErrorException.getResponseBodyAsString());
            log.error(ERROR_MESSAGE + " {}", httpClientErrorException.getLocalizedMessage(), httpClientErrorException);
            return null;
        } catch (RestClientException restClientException) {
            log.error(ERROR_MESSAGE + " {}", restClientException.getLocalizedMessage(), restClientException);
            return null;
        }
    }

    public boolean unregister(StravaToken token) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(HttpHeaders.AUTHORIZATION, String.format(BEARER_MESSAGE, token.getAccess_token()));

            final HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange("https://www.strava.com/oauth/deauthorize", HttpMethod.POST, entity, String.class);

            String gevondenToken = responseEntity.getBody();
            log.debug("Response {}", gevondenToken);
            return true;

        } catch (HttpClientErrorException httpClientErrorException) {
            log.error(httpClientErrorException.getResponseBodyAsString());
            log.error(ERROR_MESSAGE + " {}", httpClientErrorException.getLocalizedMessage(), httpClientErrorException);
            return false;
        } catch (RestClientException restClientException) {
            log.error(ERROR_MESSAGE + " {}", restClientException.getLocalizedMessage(), restClientException);
            return false;
        }
    }
}
