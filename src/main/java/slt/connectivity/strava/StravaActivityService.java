package slt.connectivity.strava;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import slt.config.StravaConfig;
import slt.connectivity.strava.dto.ListedActivityDto;
import slt.connectivity.strava.dto.SubscriptionInformation;
import slt.connectivity.strava.dto.WebhookEvent;
import slt.database.ActivityRepository;
import slt.database.SettingsRepository;
import slt.database.entities.Activity;
import slt.database.entities.Setting;
import slt.connectivity.strava.dto.StravaSyncedAccountDto;
import slt.util.LocalDateParser;

import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class StravaActivityService {

    private final SettingsRepository settingsRepository;

    private final ActivityRepository activityRepository;

    private final StravaConfig stravaConfig;

    private final StravaClient stravaClient;

    public static final String DELETED = "DELETED";

    private static final String STRAVA = "STRAVA";
    private static final String STRAVA_CLIENT_AUTHORIZATION_CODE = "STRAVA_CLIENT_AUTHORIZATION_CODE";
    private static final String STRAVA_ACCESS_TOKEN = "STRAVA_ACCESS_TOKEN";
    private static final String STRAVA_EXPIRES_AT = "STRAVA_EXPIRES_AT";
    private static final String STRAVA_REFRESH_TOKEN = "STRAVA_REFRESH_TOKEN";
    private static final String STRAVA_PROFILE = "STRAVA_PROFILE";
    private static final String STRAVA_LASTNAME = "STRAVA_LASTNAME";
    private static final String STRAVA_FIRSTNAME = "STRAVA_FIRSTNAME";
    private static final String STRAVA_ATHLETE_ID = "STRAVA_ATHLETE_ID";

    private static final String ACTIVITY = "activity";

    protected Integer stravaWebhookSubscriptionId = null;

    public StravaActivityService(final SettingsRepository settingsRepository,
                                 final ActivityRepository activityRepository,
                                 final StravaConfig stravaConfig,
                                 final StravaClient stravaClient) {
        this.settingsRepository = settingsRepository;
        this.activityRepository = activityRepository;
        this.stravaConfig = stravaConfig;
        this.stravaClient = stravaClient;

        log.debug("Setting up Strava subscription");
        if (stravaConfig == null ||
                stravaConfig.getVerifytoken() == null ||
                "uit".equals(stravaConfig.getVerifytoken())) {
            log.debug("Disable van webhook voor Strava");
        } else {
            setupStravaWebhookSubscription();
        }
    }

    // Webhook - Admin
    public SubscriptionInformation getWebhookSubscription() {
        final var clientId = stravaConfig.getClientId();
        final var clientSecret = stravaConfig.getClientSecret();
        final var subscriptionInformation = stravaClient.viewWebhookSubscription(clientId, clientSecret);
        this.stravaWebhookSubscriptionId = subscriptionInformation == null ? null : subscriptionInformation.getId();
        return subscriptionInformation;
    }

    public void endWebhookSubscription(Integer subscriptionId) {
        this.stravaWebhookSubscriptionId = null;
        final var clientId = stravaConfig.getClientId();
        final var clientSecret = stravaConfig.getClientSecret();
        stravaClient.deleteWebhookSubscription(clientId, clientSecret, subscriptionId);
    }

    private void setupStravaWebhookSubscription() {
        final var webhookSubscription = getWebhookSubscription();
        if (webhookSubscription == null) {
            log.debug("No subscription found. ");
            log.warn("Strava webhook not enabled");
        } else {
            log.debug("Subcription {} found.", webhookSubscription.getId());
            stravaWebhookSubscriptionId = webhookSubscription.getId();
        }
    }

    // User activities
    public boolean isStravaConnected(final Long userId) {
        return (settingsRepository.getLatestSetting(userId, STRAVA_ATHLETE_ID) != null);
    }

    public StravaSyncedAccountDto getStravaConnectivity(final Long userId) {
        if (isStravaConnected(userId)) {
            final var firstname = settingsRepository.getLatestSetting(userId, STRAVA_FIRSTNAME);
            final var lastname = settingsRepository.getLatestSetting(userId, STRAVA_LASTNAME);
            final var athleteId = settingsRepository.getLatestSetting(userId, STRAVA_ATHLETE_ID);
            final var image = settingsRepository.getLatestSetting(userId, STRAVA_PROFILE);

            final var stravaCount = activityRepository.countByUserIdAndSyncedWith(userId, STRAVA);

            // TODO proper optional checks
            return StravaSyncedAccountDto.builder()
                    .syncedAccountId(Long.valueOf(athleteId.getValue()))
                    .image(image.getValue())
                    .name(firstname.getValue() + " " + lastname.getValue())
                    .numberActivitiesSynced(stravaCount)
                    .build();
        } else {
            return StravaSyncedAccountDto.builder().syncedApplicationId(stravaConfig.getClientId()).build();
        }
    }

    private void saveSetting(final Long userId, final String name, final String value) {
        settingsRepository.putSetting(Setting.builder()
                .userId(userId)
                .name(name)
                .value(value)
                .day(Date.valueOf(LocalDate.now())).build());
    }

    // scope=read -- > alleen private --> geeft errors bij ophalen details
    // scope=read,activity:read_all --> moet
    public StravaSyncedAccountDto registerStravaConnectivity(final Long userId, final String clientAuthorizationCode) {
        // store user settings for this user:
        final var setting = Setting.builder()
                .userId(userId)
                .name(STRAVA_CLIENT_AUTHORIZATION_CODE)
                .value(clientAuthorizationCode)
                .day(Date.valueOf(LocalDate.now()))
                .build();
        settingsRepository.putSetting(setting);

        final var stravaToken = stravaClient.getStravaToken(clientAuthorizationCode);

        if (stravaToken != null) {
            // Initial save of all settings.
            saveSetting(userId, STRAVA_ACCESS_TOKEN, stravaToken.getAccess_token());
            saveSetting(userId, STRAVA_REFRESH_TOKEN, stravaToken.getRefresh_token());
            saveSetting(userId, STRAVA_EXPIRES_AT, stravaToken.getExpires_at().toString());
            saveSetting(userId, STRAVA_PROFILE, stravaToken.getAthlete().getProfile());
            saveSetting(userId, STRAVA_LASTNAME, stravaToken.getAthlete().getLastname());
            saveSetting(userId, STRAVA_FIRSTNAME, stravaToken.getAthlete().getFirstname());
            saveSetting(userId, STRAVA_ATHLETE_ID, stravaToken.getAthlete().getId().toString());

            final var stravaCount = activityRepository.countByUserIdAndSyncedWith(userId, STRAVA);

            return StravaSyncedAccountDto.builder()
                    .image(stravaToken.getAthlete().getProfile_medium())
                    .syncedAccountId(stravaToken.getAthlete().getId())
                    .name(stravaToken.getAthlete().getFirstname() + " " + stravaToken.getAthlete().getLastname())
                    .numberActivitiesSynced(stravaCount)
                    .build();
        } else {
            return null;
        }
    }

    public void unregisterStrava(final Long userId) {
        if (!isStravaConnected(userId)) {
            log.error("Strava has not been setup for this user");
            return;
        }
        try {
            final var token = getStravaToken(userId);
            if (token != null) {
                stravaClient.unregister(token);
            }
        } catch (Exception e) {
            log.error("Error during unregister of {} ", userId, e);
        }

        settingsRepository.deleteAllForUser(userId, STRAVA_ACCESS_TOKEN);
        settingsRepository.deleteAllForUser(userId, STRAVA_ATHLETE_ID);
        settingsRepository.deleteAllForUser(userId, STRAVA_CLIENT_AUTHORIZATION_CODE);
        settingsRepository.deleteAllForUser(userId, STRAVA_EXPIRES_AT);
        settingsRepository.deleteAllForUser(userId, STRAVA_FIRSTNAME);
        settingsRepository.deleteAllForUser(userId, STRAVA_LASTNAME);
        settingsRepository.deleteAllForUser(userId, STRAVA_PROFILE);
        settingsRepository.deleteAllForUser(userId, STRAVA_REFRESH_TOKEN);
    }

    @Transactional
    private void storeTokenSettings(final Long userId, final StravaToken stravaToken) {
        log.debug("Storing token update");

        // TODO fix nullpointer
        final var accessToken = settingsRepository.getLatestSetting(userId, STRAVA_ACCESS_TOKEN);
        final var refreshToken = settingsRepository.getLatestSetting(userId, STRAVA_REFRESH_TOKEN);
        final var expireAt = settingsRepository.getLatestSetting(userId, STRAVA_EXPIRES_AT);

        accessToken.setValue(stravaToken.getAccess_token());
        refreshToken.setValue(stravaToken.getRefresh_token());
        expireAt.setValue(stravaToken.getExpires_at().toString());

        settingsRepository.saveSetting(userId, accessToken);
        settingsRepository.saveSetting(userId, refreshToken);
        settingsRepository.saveSetting(userId, expireAt);
    }

    public List<ListedActivityDto> getStravaActivitiesForDay(StravaToken token, LocalDate date) {
        return stravaClient.getActivitiesForDay(token.getAccess_token(), date);
    }

    private StravaToken getStravaToken(final Long userId) {
        final var accessToken = settingsRepository.getLatestSetting(userId, STRAVA_ACCESS_TOKEN);
        final var refreshToken = settingsRepository.getLatestSetting(userId, STRAVA_REFRESH_TOKEN);
        final var expiresAt = settingsRepository.getLatestSetting(userId, STRAVA_EXPIRES_AT);

        if (accessToken == null || refreshToken == null || expiresAt == null) {
            log.error("Strava session not initialized");
            return null; // TODO fix
        }

        final var token = StravaToken.builder()
                .access_token(accessToken.getValue())
                .refresh_token(refreshToken.getValue())
                .expires_at(Long.valueOf(expiresAt.getValue()))
                .build();

        if (isExpired(token)) {
            log.debug("Token is expired. Refreshing...");
            final var newToken = stravaClient.refreshToken(token.getRefresh_token());
            if (newToken == null) {
                log.error("Unable to get new token");
                return null;
            } else if (isExpired(newToken)) {
                log.error("New token also expired. wtf...");
                return null;
            }
            storeTokenSettings(userId, newToken);
            return newToken;
        } else {
            return token;
        }
    }

    public List<Activity> getExtraStravaActivities(final List<Activity> dayActivities,
                                                   final Long userId,
                                                   final LocalDate date,
                                                   boolean forceUpdate) {
        final var newActivities = new ArrayList<Activity>();
        boolean webhookDisabled = this.stravaWebhookSubscriptionId == null;
        // TODO test this if
        if ((webhookDisabled || forceUpdate) && isStravaConnected(userId)) {
            log.debug("Strava is connected. Syncing");
            final var token = getStravaToken(userId);
            if (token != null) {
                final var stravaActivitiesForDay = getStravaActivitiesForDay(token, date);
                for (final var stravaActivity : stravaActivitiesForDay) {
                    checkMatchingActivities(stravaActivity, dayActivities, newActivities, token, forceUpdate);
                }
            } else {
                log.debug("No valid token");
            }
        }
        return newActivities.stream().filter(activity -> !DELETED.equals(activity.getStatus())).toList();
    }

    private void checkMatchingActivities(final ListedActivityDto stravaActivity,
                                         final List<Activity> dayActivities,
                                         final List<Activity> newActivities,
                                         final StravaToken token,
                                         boolean forceUpdate) {
        log.debug("Checking to sync {}-{} ", stravaActivity.getName(), stravaActivity.getId());
        final var stravaActivityId = stravaActivity.getId();
        final var optionalMatchingActivity = dayActivities.stream()
                .filter(a -> a.getSyncedId() != null && a.getSyncedId().equals(stravaActivityId))
                .findAny();

        if (optionalMatchingActivity.isPresent()) {
            final var matchedMacrologActivity = optionalMatchingActivity.get();
            log.debug("Activity [{}] already known", matchedMacrologActivity.getName());
            if (forceUpdate && DELETED.equals(matchedMacrologActivity.getStatus())) {
                log.debug("Setting status to back to null");
                matchedMacrologActivity.setStatus(null);
                log.debug("Refreshing the activity details");
                syncActivity(token, stravaActivityId, matchedMacrologActivity);
                activityRepository.saveActivity( matchedMacrologActivity);
            }
        } else {
            log.debug("Activity [{}] not known", stravaActivity.getName());
            final var newMacrologActivity = createNewMacrologActivity(token, stravaActivityId);
            final var savedNewActivity = activityRepository.saveActivity(newMacrologActivity);
            newActivities.add(savedNewActivity);
        }
    }

    public SubscriptionInformation startWebhookSubcription() {
        final var clientId = stravaConfig.getClientId();
        final var clientSecret = stravaConfig.getClientSecret();
        final var subscribeVerifyToken = stravaConfig.getVerifytoken();
        final var callbackUrl = stravaConfig.getCallbackUrl();
        final var subscriptionInformation = stravaClient.startWebhookSubscription(clientId, clientSecret, callbackUrl, subscribeVerifyToken);
        if (subscriptionInformation != null) {
            log.debug("Starting webhook subscription {}", subscriptionInformation.getId());
            this.stravaWebhookSubscriptionId = subscriptionInformation.getId();
        } else {
            log.error("Unable to setup Strava Webhook'");
        }
        return subscriptionInformation;
    }

    public void receiveWebhookEvent(final WebhookEvent event) {
        log.debug("'Received webhook event of owner {} for activity {} via subscription {}", event.getOwner_id(), event.getObject_id(), event.getSubscription_id());
        if (!this.stravaWebhookSubscriptionId.equals(event.getSubscription_id())) {
            log.error("Webhook event received from another subscription. Expected {}, but received {}", stravaWebhookSubscriptionId, event.getSubscription_id());
            return;
        }
        final var updates = event.getUpdates();
        if (event.getUpdates() != null) {
            for (Map.Entry<String, String> stringStringEntry : updates.entrySet()) {
                log.debug("{} - {}", stringStringEntry.getKey(), stringStringEntry.getValue());
            }
        }
        final var foundStravaUserMatch = settingsRepository.findByKeyValue(STRAVA_ATHLETE_ID, event.getOwner_id().toString());
        if (foundStravaUserMatch.isPresent()) {
            log.debug("User found " + foundStravaUserMatch.get().getUserId());
            final StravaToken stravaToken = getStravaToken(foundStravaUserMatch.get().getUserId());

            if (stravaToken == null) {
                log.error("Unable to get Strava token for {}", foundStravaUserMatch.get().getUserId());
                return;
            }

            if (ACTIVITY.equals(event.getObject_type())) {
                processStravaActivityEvent(event, foundStravaUserMatch.get(), stravaToken);
            } else {
                log.debug("Athlete events are ignored.");
            }
        } else {
            log.error("Unable to process webhook event from Strava. No user found with Strava id {}", event.getOwner_id());
        }
    }

    private void processStravaActivityEvent(final WebhookEvent event, final Setting foundStravaUserMatch, final StravaToken stravaToken) {
        final var stravaActivityId = event.getObject_id();
        final var optionalStoredStravaActivity = activityRepository.findByUserIdAndSyncIdAndSyncedWith(foundStravaUserMatch.getUserId(), STRAVA, stravaActivityId);

        if ("create".equals(event.getAspect_type()) ||
                "update".equals(event.getAspect_type())) {
            if (optionalStoredStravaActivity.isPresent()) {
                final var storedActivity = syncActivity(stravaToken, stravaActivityId, optionalStoredStravaActivity.get());
                activityRepository.saveActivity( storedActivity);
                log.debug("Strava activity updated");
            } else {
                final var newMacrologActivity = createNewMacrologActivity(stravaToken, stravaActivityId);
                activityRepository.saveActivity(newMacrologActivity);
                log.debug("New activity added via strava {}", stravaActivityId);
            }
        } else if ("delete".equals(event.getAspect_type())) {
            if (optionalStoredStravaActivity.isPresent()) {
                activityRepository.deleteActivity(foundStravaUserMatch.getUserId(), optionalStoredStravaActivity.get().getId());
                log.debug("Deleted Activity {}", optionalStoredStravaActivity.get().getId());
            } else {
                log.debug("Unable to delete Strava Activity {}. It was not synced.", stravaActivityId);
            }
        }
    }

    private Activity createNewMacrologActivity(final StravaToken token, final Long stravaActivityId) {
        final var activityDetail = stravaClient.getActivityDetail(token.getAccess_token(), stravaActivityId);
        final var startDateString = activityDetail.getStart_date();
        // To avoid timezone issues we take the date part only and convert it to localdate
        final var startDateLocalDate = LocalDateParser.parse(startDateString.substring(0, startDateString.indexOf('T')));
        return Activity.builder()
                .day(Date.valueOf(startDateLocalDate))
                .name(makeUTF8(activityDetail.getType() + ": " + activityDetail.getName()))
                .calories(activityDetail.getCalories())
                .syncedId(stravaActivityId)
                .syncedWith(STRAVA)
                .build();
    }

    private String makeUTF8(final String original) {
        byte[] p = original.getBytes(StandardCharsets.ISO_8859_1);
        return new String(p, StandardCharsets.UTF_8);
    }

    private Activity syncActivity(final StravaToken token, final Long stravaActivityId, final Activity storedActivity) {
        final var activityDetail = stravaClient.getActivityDetail(token.getAccess_token(), stravaActivityId);
        final var name = activityDetail.getType() + ": " + activityDetail.getName();
        storedActivity.setName(makeUTF8(name));
        storedActivity.setCalories(activityDetail.getCalories());
        return storedActivity;
    }

    private boolean isExpired(final StravaToken token) {
        final var expires_at = token.getExpires_at();
        final var instant = Instant.ofEpochSecond(expires_at);
        final var timeTokenExpires = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        final var currentTime = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(10);
        log.debug("Token valid until [{}]", timeTokenExpires);
        return timeTokenExpires.isBefore(currentTime);
    }
}
