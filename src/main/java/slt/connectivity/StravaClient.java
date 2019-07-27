package slt.connectivity;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    public StravaToken getStravaToken(String authorizationCode) {
        String grant_type = "authorization_code";
        String tokenUrl = "https://www.strava.com/oauth/token";

        String clientId = stravaConfig.getClientId().toString();
        String clientSecret = stravaConfig.getClientSecret();
        Map req_payload = new HashMap();
        req_payload.put("client_id", clientId);
        req_payload.put("client_secret", clientSecret);
        req_payload.put("code", authorizationCode);
        req_payload.put("grant_type", grant_type);

        return getStravaToken(tokenUrl, req_payload);

    }

    private StravaToken getStravaToken(String tokenUrl, Map req_payload) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            final HttpEntity<HashMap> entity = new HttpEntity(req_payload, headers);
            ResponseEntity<StravaToken> responseEntity = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, StravaToken.class);

            StravaToken gevondenToken = responseEntity.getBody();
            log.debug("Gevonden token {}", gevondenToken.access_token);
            return gevondenToken;

        } catch (RestClientException restClientException) {
            log.error("Fout bij versturen. {}", restClientException.getLocalizedMessage(), restClientException);
            if (restClientException instanceof HttpClientErrorException) {
                log.error(((HttpClientErrorException) restClientException).getResponseBodyAsString());
            }
            return null;
        }
    }

    public StravaToken refreshToken(String refreshToken) {
        String tokenUrl = "https://www.strava.com/oauth/token";
        String grant_type = "refresh_token";

        String clientId = stravaConfig.getClientId().toString();
        String clientSecret = stravaConfig.getClientSecret();

        Map req_payload = new HashMap();
        req_payload.put("client_id", clientId);
        req_payload.put("client_secret", clientSecret);
        req_payload.put("refresh_token", refreshToken);
        req_payload.put("grant_type", grant_type);

        return getStravaToken(tokenUrl, req_payload);

//        try {
//            final HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            final HttpEntity<StravaToken> entity = new HttpEntity(req_payload, headers);
//            ResponseEntity<StravaToken> responseEntity = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, StravaToken.class);
//
//            return responseEntity.getBody();
//
//        } catch (RestClientException restClientException) {
//            log.error(((HttpClientErrorException) restClientException).getResponseBodyAsString());
//            log.error("Fout bij versturen.", restClientException.getLocalizedMessage());
//            restClientException.printStackTrace();
//            return null;
//        }
    }


//    public void getUserInfo() {
//        try {
//            String url = "https://www.strava.com/api/v3/athlete";
//            String userToken = "7c4e26b2b9788f8cdeb57befc3993e3ac57d2b96";
//
//            final HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.add("Authorization", String.format("Bearer %s", userToken));
//
//            final HttpEntity<String> entity = new HttpEntity<>(headers);
//            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//
//            String gevondenActivity = responseEntity.getBody();
//
//            log.debug(gevondenActivity);
//
//        } catch (RestClientException restClientException) {
//            log.error("Fout bij versturen.", restClientException.getLocalizedMessage());
//        }
//    }

    public List<ListedActivityDto> getActivitiesForDay(String token, LocalDate date) {
        try {
            String url = "https://www.strava.com/api/v3/athlete/activities";

            final LocalDateTime localDateTimeStartOfDay = date.atStartOfDay();
            final LocalDateTime localDateTimeEndOfDay = date.atStartOfDay().plusDays(1);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("before", localDateTimeEndOfDay.atZone(ZoneId.of("UTC")).toEpochSecond())
                    .queryParam("after", localDateTimeStartOfDay.atZone(ZoneId.of("UTC")).toEpochSecond());

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", String.format("Bearer %s", token));

            final HttpEntity entity = new HttpEntity<>(headers);
            ParameterizedTypeReference<List<ListedActivityDto>> parameterizedTypeReference = new ParameterizedTypeReference<List<ListedActivityDto>>() {
            };
            ResponseEntity<List<ListedActivityDto>> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, parameterizedTypeReference);

            List<ListedActivityDto> gevondenActivities = responseEntity.getBody();
            return gevondenActivities;
        } catch (RestClientException restClientException) {

//            {"message":"Authorization Error","errors":[{"resource":"AccessToken","field":"activity:read_permission","code":"missing"}]}
            log.error(((HttpClientErrorException) restClientException).getResponseBodyAsString());
            log.error("Fout bij versturen. {}", restClientException.getLocalizedMessage(), restClientException);
            return null;
        }
    }

    public ActivityDetailsDto getActivityDetail(String token, Long activityDetailId) {
        try {
            String url = "https://www.strava.com/api/v3/activities";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + "/" + activityDetailId);

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", String.format("Bearer %s", token));

            final HttpEntity<ActivityDetailsDto> entity = new HttpEntity<>(headers);
            ResponseEntity<ActivityDetailsDto> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, ActivityDetailsDto.class);

            ActivityDetailsDto gevondenActivity = responseEntity.getBody();

            log.debug(gevondenActivity.getStart_date_local() + " - " + gevondenActivity.getCalories() + " - " + gevondenActivity.getName() + " " + gevondenActivity.type + " " + gevondenActivity.getId());
            return gevondenActivity;

        } catch (RestClientException restClientException) {

//            {"message":"Authorization Error","errors":[{"resource":"AccessToken","field":"activity:read_permission","code":"missing"}]}
            log.error(((HttpClientErrorException) restClientException).getResponseBodyAsString());
            log.error("Fout bij versturen. {}", restClientException.getLocalizedMessage(), restClientException);
            return null;
        }
    }
}
