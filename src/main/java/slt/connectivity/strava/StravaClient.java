package slt.connectivity.strava;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import slt.config.StravaConfig;
import slt.connectivity.strava.dto.ActivityDetailsDto;
import slt.connectivity.strava.dto.ListedActivityDto;
import slt.connectivity.strava.dto.SubscriptionInformation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * See <a href="https://developers.strava.com/docs/authentication/">Strava Documentation</a> for implementation used.
 */
@Slf4j
@Component
@AllArgsConstructor
@NoArgsConstructor
public class StravaClient {

    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String CALLBACK_URL = "callback_url";
    public static final String VERIFY_TOKEN = "verify_token";
    public static final String GRANT_TYPE = "grant_type";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StravaConfig stravaConfig;

    private static final String ERROR_MESSAGE = "Fout bij versturen.";
    private static final String BEARER_MESSAGE = "Bearer %s";

    private static final String STRAVA_WEBHOOK_URL = "https://www.strava.com/api/v3/push_subscriptions";
    private static final String STRAVA_ACTIVITIES_URL = "https://www.strava.com/api/v3/activities";
    private static final String STRAVA_AUTHENTICATION_URL = "https://www.strava.com/oauth/token";

    public Optional<StravaToken> getStravaToken(final String authorizationCode) {
        final var grantType = "authorization_code";
        final var clientId = stravaConfig.getClientId().toString();
        final var clientSecret = stravaConfig.getClientSecret();

        final var reqPayload = new HashMap<String, String>();
        reqPayload.put(CLIENT_ID, clientId);
        reqPayload.put(CLIENT_SECRET, clientSecret);
        reqPayload.put("code", authorizationCode);
        reqPayload.put(GRANT_TYPE, grantType);

        return getStravaToken(reqPayload);
    }

    public Optional<StravaToken> refreshToken(final String refreshToken) {
        final var grantType = "refresh_token";
        final var clientId = stravaConfig.getClientId().toString();
        final var clientSecret = stravaConfig.getClientSecret();

        final var reqPayload = new HashMap<String, String>();
        reqPayload.put(CLIENT_ID, clientId);
        reqPayload.put(CLIENT_SECRET, clientSecret);
        reqPayload.put("refresh_token", refreshToken);
        reqPayload.put(GRANT_TYPE, grantType);

        return getStravaToken(reqPayload);
    }

    public List<ListedActivityDto> getActivitiesForDay(final String token, final LocalDate date) {
        try {
            final var localDateTimeStartOfDay = date.atStartOfDay();
            final var localDateTimeEndOfDay = date.atStartOfDay().plusDays(1);

            final var builder = UriComponentsBuilder.fromHttpUrl(STRAVA_ACTIVITIES_URL)
                    .queryParam("before", getUTCEpoch(localDateTimeEndOfDay))
                    .queryParam("after", getUTCEpoch(localDateTimeStartOfDay));

            final var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(HttpHeaders.AUTHORIZATION, String.format(BEARER_MESSAGE, token));

            final var entity = new HttpEntity<>(headers);
            final var parameterizedTypeReference = new ParameterizedTypeReference<List<ListedActivityDto>>() {
            };
            final var responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, parameterizedTypeReference);
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

    public Optional<ActivityDetailsDto> getActivityDetail(final String token, final Long activityDetailId) {
        try {
            final var builder = UriComponentsBuilder.fromHttpUrl(STRAVA_ACTIVITIES_URL + "/" + activityDetailId);
            final var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(HttpHeaders.AUTHORIZATION, String.format(BEARER_MESSAGE, token));

            final var entity = new HttpEntity<>(headers);
            final var responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, ActivityDetailsDto.class);

            final var gevondenActivity = responseEntity.getBody();
            if (gevondenActivity != null) {
                log.debug("{} - {} - {} {} {}", gevondenActivity.getStart_date(), gevondenActivity.getCalories(), gevondenActivity.getName(), gevondenActivity.getType(), gevondenActivity.getId());
                return Optional.of(gevondenActivity);
            } else {
                log.debug("Gevonden activity: " + null);
                return Optional.empty();
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            log.error(httpClientErrorException.getResponseBodyAsString());
            log.error(ERROR_MESSAGE + " {}", httpClientErrorException.getLocalizedMessage(), httpClientErrorException);
            return Optional.empty();
        } catch (RestClientException restClientException) {
            log.error(ERROR_MESSAGE + " {}", restClientException.getLocalizedMessage(), restClientException);
            return Optional.empty();
        }
    }

    public boolean unregister(final StravaToken token) {
        try {
            final var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(HttpHeaders.AUTHORIZATION, String.format(BEARER_MESSAGE, token.getAccess_token()));
            final var entity = new HttpEntity<>(headers);
            final var responseEntity = restTemplate.exchange("https://www.strava.com/oauth/deauthorize", HttpMethod.POST, entity, String.class);
            final var gevondenToken = responseEntity.getBody();
            return gevondenToken != null;
        } catch (HttpClientErrorException httpClientErrorException) {
            log.error(httpClientErrorException.getResponseBodyAsString());
            log.error(ERROR_MESSAGE + " {}", httpClientErrorException.getLocalizedMessage(), httpClientErrorException);
            return false;
        } catch (RestClientException restClientException) {
            log.error(ERROR_MESSAGE + " {}", restClientException.getLocalizedMessage(), restClientException);
            return false;
        }
    }

    public Optional<SubscriptionInformation> startWebhookSubscription(final Integer clientId,
                                                                      final String clientSecret,
                                                                      final String callbackUrl,
                                                                      final String subscribeVerifyToken) {
        final var reqPayload = new LinkedMultiValueMap<String, String>();
        reqPayload.add(CLIENT_ID, clientId.toString());
        reqPayload.add(CLIENT_SECRET, clientSecret);
        reqPayload.add(CALLBACK_URL, callbackUrl);
        reqPayload.add(VERIFY_TOKEN, subscribeVerifyToken);

        try {
            final var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            final var entity = new HttpEntity<MultiValueMap<String, String>>(reqPayload, headers);
            final var responseEntity = restTemplate.exchange(STRAVA_WEBHOOK_URL, HttpMethod.POST, entity, SubscriptionInformation.class);
            final var subscription = responseEntity.getBody();
            if (subscription != null) {
                log.debug("Aangemaakte subscription {}", subscription.getId());
                return Optional.of(subscription);
            } else {
                log.debug("Subscription: " + null);
                return Optional.empty();
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            log.error(httpClientErrorException.getResponseBodyAsString());
            log.error(ERROR_MESSAGE + " {}", httpClientErrorException.getLocalizedMessage(), httpClientErrorException);
            return Optional.empty();
        } catch (RestClientException restClientException) {
            log.error(ERROR_MESSAGE + " {}", restClientException.getLocalizedMessage(), restClientException);
            return Optional.empty();
        }
    }

    public Optional<SubscriptionInformation> viewWebhookSubscription(final Integer clientId, final String clientSecret) {
        try {
            final var builder = UriComponentsBuilder.fromHttpUrl(STRAVA_WEBHOOK_URL)
                    .queryParam(CLIENT_ID, clientId)
                    .queryParam(CLIENT_SECRET, clientSecret);
            final var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            final var entity = new HttpEntity<HttpHeaders>(headers);
            final var parameterizedTypeReference = new ParameterizedTypeReference<List<SubscriptionInformation>>() {
            };

            final var responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, parameterizedTypeReference);
            final var body = responseEntity.getBody();
            if (body != null && !body.isEmpty()) {
                return Optional.of(body.getFirst());
            } else {
                return Optional.empty();
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            log.error(httpClientErrorException.getResponseBodyAsString());
            log.error(ERROR_MESSAGE + " {}", httpClientErrorException.getLocalizedMessage(), httpClientErrorException);
            return Optional.empty();
        } catch (RestClientException restClientException) {
            log.error(ERROR_MESSAGE + " {}", restClientException.getLocalizedMessage(), restClientException);
            return Optional.empty();
        }
    }

    public void deleteWebhookSubscription(final Integer clientId, final String clientSecret, final Integer subscriptionId) {
        try {
            final var builder = UriComponentsBuilder.fromHttpUrl(STRAVA_WEBHOOK_URL + "/" + subscriptionId);
            final var reqPayload = new LinkedMultiValueMap<String, Object>();
            reqPayload.add(CLIENT_ID, clientId);
            reqPayload.add(CLIENT_SECRET, clientSecret);

            final var headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            final var entity = new HttpEntity<>(reqPayload, headers);
            final var responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, String.class);
            log.debug("Received response for delete subscription : {}", responseEntity.getStatusCode());
        } catch (HttpClientErrorException httpClientErrorException) {
            log.error(httpClientErrorException.getResponseBodyAsString());
            log.error(ERROR_MESSAGE + " {}", httpClientErrorException.getLocalizedMessage(), httpClientErrorException);
        } catch (RestClientException restClientException) {
            log.error(ERROR_MESSAGE + " {}", restClientException.getLocalizedMessage(), restClientException);
        }
    }

    private Optional<StravaToken> getStravaToken(final Map<String, String> reqPayload) {
        try {
            final var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            final var entity = new HttpEntity<>(reqPayload, headers);
            final var responseEntity = restTemplate.exchange(STRAVA_AUTHENTICATION_URL, HttpMethod.POST, entity, StravaToken.class);
            if (responseEntity.getBody() != null) {
                return Optional.of(responseEntity.getBody());
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            log.error(httpClientErrorException.getResponseBodyAsString());
            log.error(ERROR_MESSAGE + " {}", httpClientErrorException.getLocalizedMessage(), httpClientErrorException);
        } catch (RestClientException restClientException) {
            log.error(ERROR_MESSAGE + " {}", restClientException.getLocalizedMessage(), restClientException);
        }
        return Optional.empty();
    }

    private long getUTCEpoch(final LocalDateTime localDateTime) {
        final var ldtZoned = localDateTime.atZone(ZoneId.systemDefault());
        final var utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
        return utcZoned.toEpochSecond();
    }

}
