package slt.connectivity.strava;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import slt.config.StravaConfig;
import slt.connectivity.strava.dto.ActivityDetailsDto;
import slt.connectivity.strava.dto.ListedActivityDto;
import slt.connectivity.strava.dto.SubscriptionInformation;
import slt.connectivity.strava.dto.WebhookEvent;
import slt.database.ActivityRepository;
import slt.database.SettingsRepository;
import slt.database.entities.LogActivity;
import slt.database.entities.Setting;
import slt.dto.SyncedAccount;
import slt.rest.ActivityService;

import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class StravaActivityService {

    SettingsRepository settingsRepository;

    ActivityRepository activityRepository;

    StravaConfig stravaConfig;

    StravaClient stravaClient;

    private static final String STRAVA = "STRAVA";
    private static final String STRAVA_CLIENT_AUTHORIZATION_CODE = "STRAVA_CLIENT_AUTHORIZATION_CODE";
    private static final String STRAVA_ACCESS_TOKEN = "STRAVA_ACCESS_TOKEN";
    private static final String STRAVA_EXPIRES_AT = "STRAVA_EXPIRES_AT";
    private static final String STRAVA_REFRESH_TOKEN = "STRAVA_REFRESH_TOKEN";
    private static final String STRAVA_PROFILE = "STRAVA_PROFILE";
    private static final String STRAVA_LASTNAME = "STRAVA_LASTNAME";
    private static final String STRAVA_FIRSTNAME = "STRAVA_FIRSTNAME";
    private static final String STRAVA_ATHLETE_ID = "STRAVA_ATHLETE_ID";

    Integer stravaSubscriptionId = null;

    public StravaActivityService(SettingsRepository settingsRepository,
                                 ActivityRepository activityRepository,
                                 StravaConfig stravaConfig,
                                 StravaClient stravaClient) {
        this.settingsRepository = settingsRepository;
        this.activityRepository=activityRepository;
        this.stravaConfig=stravaConfig;
        this.stravaClient=stravaClient;

        log.debug("Setting up Strava subscription");
        if ("uit".equals(stravaConfig.getVerifytoken())){
            log.debug("Disable van webhook voor Strava");
        } else {
            setupStravaWebhooksubscription();
        }
    }

    private void setupStravaWebhooksubscription() {
        final SubscriptionInformation webhookSubscription = getWebhookSubscription();
        if (webhookSubscription == null) {
            log.debug("No subscription found. ");
            log.warn("Strava webhook not enabled");
        } else {
            if (webhookSubscription != null) {
                log.debug("Subcription {} found.", webhookSubscription.getId());
                stravaSubscriptionId = webhookSubscription.getId();
            }
        }
    }

    public boolean isStravaConnected(Integer userId) {
        return (settingsRepository.getLatestSetting(userId, STRAVA_ATHLETE_ID) != null);
    }

    public SyncedAccount getStravaConnectivity(Integer userId) {
        if (isStravaConnected(userId)) {

            final Setting firstname = settingsRepository.getLatestSetting(userId, STRAVA_FIRSTNAME);
            final Setting lastname = settingsRepository.getLatestSetting(userId, STRAVA_LASTNAME);
            final Setting athletId = settingsRepository.getLatestSetting(userId, STRAVA_ATHLETE_ID);
            final Setting image = settingsRepository.getLatestSetting(userId, STRAVA_PROFILE);

            final Long stravaCount = activityRepository.countByUserIdAndSyncedWith(userId, STRAVA);

            return SyncedAccount.builder()
                    .syncedAccountId(Long.valueOf(athletId.getValue()))
                    .image(image.getValue())
                    .name(firstname.getValue() + " " + lastname.getValue())
                    .numberActivitiesSynced(stravaCount)
                    .build();
        } else {
            return SyncedAccount.builder().syncedApplicationId(stravaConfig.getClientId()).build();
        }
    }


    private void saveSetting(Integer userId, String name, String value){

        settingsRepository.putSetting(userId,Setting.builder()
                .userId(userId)
                .name(name)
                .value(value)
                .day(Date.valueOf(LocalDate.now())).build());
    }
    // scope=read -- > alleen private --> geeft errors bij ophalen details
    // scope=read,activity:read_all --> moet
    public SyncedAccount registerStravaConnectivity(Integer userId, String clientAuthorizationCode) {
        // store user settings for this user:

        Setting setting = Setting.builder()
                .name(STRAVA_CLIENT_AUTHORIZATION_CODE)
                .value(clientAuthorizationCode)
                .day(Date.valueOf(LocalDate.now()))
                .build();
        settingsRepository.putSetting(userId, setting);

        StravaToken stravaToken = stravaClient.getStravaToken(clientAuthorizationCode);

        if (stravaToken != null) {

            // Initial save of all settings.

            saveSetting(userId, STRAVA_ACCESS_TOKEN, stravaToken.getAccess_token());
            saveSetting(userId, STRAVA_REFRESH_TOKEN, stravaToken.getRefresh_token());
            saveSetting(userId, STRAVA_EXPIRES_AT, stravaToken.getExpires_at().toString());
            saveSetting(userId, STRAVA_PROFILE, stravaToken.getAthlete().getProfile());
            saveSetting(userId, STRAVA_LASTNAME, stravaToken.getAthlete().getLastname());
            saveSetting(userId, STRAVA_FIRSTNAME, stravaToken.getAthlete().getFirstname());
            saveSetting(userId, STRAVA_ATHLETE_ID, stravaToken.getAthlete().getId().toString());

            final Long stravaCount = activityRepository.countByUserIdAndSyncedWith(userId, STRAVA);

            return SyncedAccount.builder()
                    .image(stravaToken.getAthlete().getProfile_medium())
                    .syncedAccountId(stravaToken.getAthlete().getId())
                    .name(stravaToken.getAthlete().getFirstname() + " " + stravaToken.getAthlete().getLastname())
                    .numberActivitiesSynced(stravaCount)
                    .build();
        } else {
            return null;
        }
    }

    public void unRegisterStrava(Integer userId) {
        if (!isStravaConnected(userId)) {
            log.error("Strava has not been setup for this user");
            return;
        }
        try {
            StravaToken token = getStravaToken(userId);
            stravaClient.unregister(token);
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
    private void storeTokenSettings(Integer userId, StravaToken stravaToken) {
        log.debug("Storing token update");

        final Setting accessToken = settingsRepository.getLatestSetting(userId, STRAVA_ACCESS_TOKEN);
        final Setting refreshToken = settingsRepository.getLatestSetting(userId, STRAVA_REFRESH_TOKEN);
        final Setting expireAt = settingsRepository.getLatestSetting(userId, STRAVA_EXPIRES_AT);

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

    private StravaToken getStravaToken(Integer userId) {
        final Setting accessToken = settingsRepository.getLatestSetting(userId, STRAVA_ACCESS_TOKEN);
        final Setting refreshToken = settingsRepository.getLatestSetting(userId, STRAVA_REFRESH_TOKEN);
        final Setting expiresAt = settingsRepository.getLatestSetting(userId, STRAVA_EXPIRES_AT);

        if (accessToken == null ||
                refreshToken == null ||
                expiresAt == null) {
            log.error("Strava session not initialized");
            return null;
        }
        StravaToken token = StravaToken.builder()
                .access_token(accessToken.getValue())
                .refresh_token(refreshToken.getValue())
                .expires_at(Long.valueOf(expiresAt.getValue()))
                .build();

        if (isExpired(token)) {
            log.debug("Token is expired. Refreshing..");
            token = stravaClient.refreshToken(token.getRefresh_token());
            if (token == null) {
                log.error("Unable to get new token");
                return null;
            } else if (isExpired(token)) {
                log.error("New token also expired. wtf...");
                return null;
            }
            storeTokenSettings(userId, token);
        }
        return token;
    }

    public List<LogActivity> getExtraStravaActivities(List<LogActivity> dayActivities,
                                                      Integer userId,
                                                      LocalDate date,
                                                      boolean forceUpdate) {
        List<LogActivity> newActivities = new ArrayList<>();
        if (isStravaConnected(userId)) {

            log.debug("Strava is connected. Syncing");
            StravaToken token = getStravaToken(userId);
            if (token != null) {
                final List<ListedActivityDto> stravaActivitiesForDay = getStravaActivitiesForDay(token, date);

                for (ListedActivityDto stravaActivity : stravaActivitiesForDay) {
                    log.debug("Checking to sync {}-{} ", stravaActivity.getName(), stravaActivity.getId());
                    final long stravaActivityId = stravaActivity.getId();
                    final Optional<LogActivity> matchingMacrologActivity = dayActivities.stream()
                            .filter(a -> a.getSyncedId() != null && a.getSyncedId() == stravaActivityId)
                            .findAny();

                    if (matchingMacrologActivity.isPresent()) {
                        final LogActivity matchedMacrologActivity = matchingMacrologActivity.get();
                        log.debug("Activity [{}] already known", matchedMacrologActivity.getName());
                        if (forceUpdate && "DELETED".equals(matchedMacrologActivity.getStatus())) {
                            log.debug("Setting status to back to null");
                            matchedMacrologActivity.setStatus(null);
                            log.debug("Refreshing the activity details");
                            syncActivity(token, stravaActivityId, matchedMacrologActivity);
                            activityRepository.saveActivity(userId, matchedMacrologActivity);
                        }
                    } else {
                        log.debug("Activity [{}] not known");
                        final LogActivity newMacrologActivity = createNewMacrologActivity(token, stravaActivityId);
                        final LogActivity savedNewActivity = activityRepository.saveActivity(userId, newMacrologActivity);
                        newActivities.add(savedNewActivity);
                    }
                }
            } else {
                log.debug("No valid token");
            }
        }
        return newActivities;
    }

    public SubscriptionInformation startWebhookSubcription(){
        final Integer clientId = stravaConfig.getClientId();
        final String clientSecret = stravaConfig.getClientSecret();
        final String subscribeVerifyToken = stravaConfig.getVerifytoken();
        final String callbackUrl = stravaConfig.getCallbackUrl();
        final SubscriptionInformation subscriptionInformation = stravaClient.startWebhookSubscription(clientId, clientSecret, callbackUrl, subscribeVerifyToken);
        if (subscriptionInformation != null){
            log.debug("Starting webhook subscription {}", subscriptionInformation.getId());
            this.stravaSubscriptionId = subscriptionInformation.getId();
        } else {
            log.error("Unable to setup Strava Webhook'");
        }
        return subscriptionInformation;
    }

    public SubscriptionInformation getWebhookSubscription(){
        final Integer clientId = stravaConfig.getClientId();
        final String clientSecret = stravaConfig.getClientSecret();
        return stravaClient.viewWebhookSubscription(clientId, clientSecret);
    }
    public boolean endWebhookSubscription(Integer subscriptionId){
        final Integer clientId = stravaConfig.getClientId();
        final String clientSecret = stravaConfig.getClientSecret();
        return stravaClient.deleteWebhookSubscription(clientId, clientSecret,subscriptionId);
    }

    public void receiveWebHookEvent(WebhookEvent event) {
        log.debug("'Received webhook event of {}", event.getOwner_id());
        if (this.stravaSubscriptionId != event.getSubscription_id()){
            log.error("Webhook event received from another subscription. Exceptec {}, but received {}",stravaSubscriptionId,event.getSubscription_id());
            return;
        }
        final HashMap<String, String> updates = event.getUpdates();
        for (Map.Entry<String, String> stringStringEntry : updates.entrySet()) {
            log.debug(stringStringEntry.getKey() + " - " + stringStringEntry.getValue());
        }
        final Optional<Setting> foundStravaUserMatch = settingsRepository.findByKeyValue(STRAVA_ATHLETE_ID, event.getOwner_id().toString());
        if (foundStravaUserMatch.isPresent()) {
            log.debug("User found " + foundStravaUserMatch.get().getUserId());
            final StravaToken stravaToken = getStravaToken(foundStravaUserMatch.get().getUserId());

            if ("activity".equals(event.getObject_type())){
                Long stravaActivityId = event.getObject_id();

                final Optional<LogActivity> storedStrava = activityRepository.findByUserIdAndSyncIdAndSyncedWith(foundStravaUserMatch.get().getUserId(), "STRAVA", stravaActivityId);

                if ("create".equals(event.getAspect_type()) ||
                        "update".equals(event.getAspect_type())){
                    // check if not already exists

                    if (storedStrava.isPresent()) {
                        final LogActivity storedActivity = storedStrava.get();
                        syncActivity(stravaToken,stravaActivityId , storedActivity);
                        activityRepository.saveActivity(foundStravaUserMatch.get().getUserId(), storedActivity);
                        log.debug("Strava activity updated");
                    } else {
                        final LogActivity newMacrologActivity = createNewMacrologActivity(stravaToken, stravaActivityId);
                        activityRepository.saveActivity(foundStravaUserMatch.get().getUserId(), newMacrologActivity);
                        log.debug("New activity added via strava {}", stravaActivityId);
                    }
                } else if ("delete".equals(event.getAspect_type())) {
                    // delete activty
                    if (storedStrava.isPresent()) {
                        log.debug("Delete Activity {}" , storedStrava.get().getId());
                        activityRepository.deleteLogActivity(foundStravaUserMatch.get().getUserId(), storedStrava.get().getId());
                    } else {
                        log.debug("Unable to delete Activity {}. It was not synced." , storedStrava.get().getId());
                    }
                }

            } else {
                log.debug("Athlete events are ignored.");
            }
        } else {
            log.error("Unable to process webhook event from Strava. No user found with Strava id {}", event.getOwner_id());
        }

    }

    private LogActivity createNewMacrologActivity(StravaToken token, Long stravaActivityId) {
        final ActivityDetailsDto activityDetail = stravaClient.getActivityDetail(token.getAccess_token(), stravaActivityId);
        final DateTime start_date_local = activityDetail.getStart_date_local();
        return LogActivity.builder()
                .day(new Date(start_date_local.getMillis())) // TODO testme silly!
                .name(makeUTF8(activityDetail.getType() + ": " + activityDetail.getName()))
                .calories(activityDetail.getCalories())
                .syncedId(stravaActivityId)
                .syncedWith(STRAVA)
                .build();
    }

    private String makeUTF8(String original){
        try {

            byte[] p = original.getBytes("ISO-8859-1");
            String ret = new String(p,"UTF-8");
            return ret;
        } catch (UnsupportedEncodingException e) {
            log.error("Unable to make text UTF {}",original,e);
            return "--";
        }
    }
    private void syncActivity(StravaToken token, Long stravaActivityId, LogActivity storedActivity) {

        final ActivityDetailsDto activityDetail = stravaClient.getActivityDetail(token.getAccess_token(), stravaActivityId);

        String name = activityDetail.getType() + ": " + activityDetail.getName();

        storedActivity.setName(makeUTF8(name));
        storedActivity.setCalories(activityDetail.getCalories());
    }

    private boolean isExpired(StravaToken token) {
        final Long expires_at = token.getExpires_at();
        Instant instant = Instant.ofEpochSecond(expires_at);
        LocalDateTime timeTokenExpires = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime currentTime = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(10);
        log.debug("Token valid until [{}]", timeTokenExpires);
        return timeTokenExpires.isBefore(currentTime);
    }
}
