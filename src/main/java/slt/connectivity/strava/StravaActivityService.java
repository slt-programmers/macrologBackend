package slt.connectivity.strava;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import slt.config.StravaConfig;
import slt.connectivity.strava.dto.ActivityDetailsDto;
import slt.connectivity.strava.dto.ListedActivityDto;
import slt.database.ActivityRepository;
import slt.database.SettingsRepository;
import slt.database.entities.LogActivity;
import slt.database.entities.Setting;
import slt.dto.SyncedAccount;
import slt.rest.ActivityService;

import javax.transaction.Transactional;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StravaActivityService {

    @Autowired
    SettingsRepository settingsRepository;

    @Autowired
    ActivityService activityService;

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    StravaConfig stravaConfig;

    @Autowired
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
            settingsRepository.putSetting(userId, STRAVA_ACCESS_TOKEN, stravaToken.getAccess_token(), null);
            settingsRepository.putSetting(userId, STRAVA_REFRESH_TOKEN, stravaToken.getRefresh_token(), null);
            settingsRepository.putSetting(userId, STRAVA_EXPIRES_AT, stravaToken.getExpires_at().toString(), null);
            settingsRepository.putSetting(userId, STRAVA_PROFILE, stravaToken.getAthlete().getProfile(), null);
            settingsRepository.putSetting(userId, STRAVA_LASTNAME, stravaToken.getAthlete().getLastname(), null);
            settingsRepository.putSetting(userId, STRAVA_FIRSTNAME, stravaToken.getAthlete().getFirstname(), null);
            settingsRepository.putSetting(userId, STRAVA_ATHLETE_ID, stravaToken.getAthlete().getId().toString(), null);


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
                    final long id = stravaActivity.getId();
                    final Optional<LogActivity> matchingMacrologActivity = dayActivities.stream()
                            .filter(a -> a.getSyncedId() != null && a.getSyncedId() == id)
                            .findAny();

                    if (matchingMacrologActivity.isPresent()) {
                        final LogActivity matchedMacrologActivity = matchingMacrologActivity.get();
                        log.debug("Activity [{}] already known", matchedMacrologActivity.getName());
                        if (forceUpdate && "DELETED".equals(matchedMacrologActivity.getStatus())) {
                            log.debug("Setting status to back to null");
                            matchedMacrologActivity.setStatus(null);
                            log.debug("Refreshing the activity details");
                            syncActivity(token, id, matchedMacrologActivity);
                            activityRepository.saveActivity(userId, matchedMacrologActivity);
                        }
                    } else {
                        log.debug("Activity [{}] not known");
                        final LogActivity newMacrologActivity = createNewMacrologActivity(date, token, stravaActivity, id);
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

    private LogActivity createNewMacrologActivity(LocalDate date, StravaToken token, ListedActivityDto stravaActivity, long id) {
        final ActivityDetailsDto activityDetail = stravaClient.getActivityDetail(token.getAccess_token(), id);
        return LogActivity.builder()
                .day(Date.valueOf(date))
                .name(stravaActivity.getType() + ": " + stravaActivity.getName())
                .calories(activityDetail.getCalories())
                .syncedId(stravaActivity.getId())
                .syncedWith(STRAVA)
                .build();
    }

    private void syncActivity(StravaToken token, Long stravaActivityId, LogActivity storedActivity) {
        final ActivityDetailsDto activityDetail = stravaClient.getActivityDetail(token.getAccess_token(), stravaActivityId);
        storedActivity.setName(activityDetail.getType() + ": " + activityDetail.getName());
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
