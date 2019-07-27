package slt.connectivity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import slt.database.ActivityRepository;
import slt.database.SettingsRepository;
import slt.database.entities.LogActivity;
import slt.database.entities.Setting;
import slt.dto.SyncedAccount;
import slt.rest.ActivityService;

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

    // TODO via service
    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    StravaClient stravaClient;

    private static final String STRAVA_CLIENT_AUTHORIZATION_CODE = "STRAVA_CLIENT_AUTHORIZATION_CODE";
    private static final String STRAVA_ACCESS_TOKEN = "STRAVA_ACCESS_TOKEN";
    private static final String STRAVA_EXPIRES_AT = "STRAVA_EXPIRES_AT";
    private static final String STRAVA_REFRESH_TOKEN = "STRAVA_REFRESH_TOKEN";
    private static final String STRAVA_PROFILE_MEDIUM = "STRAVA_PROFILE_MEDIUM";
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
            final Setting image = settingsRepository.getLatestSetting(userId, STRAVA_PROFILE_MEDIUM);

            return SyncedAccount.builder()
                    .syncedAccountId(Long.valueOf(athletId.getValue()))
                    .image(image.getValue())
                    .name(firstname.getValue() + " " + lastname.getValue())
                    .build();
        } else {
            return null;
        }
    }

    public SyncedAccount registerStravaConnectivity(Integer userId, String clientAuthorizationCode) {
        // store user settings for this user:

        Setting setting = Setting.builder()
                .name(STRAVA_CLIENT_AUTHORIZATION_CODE)
                .value(clientAuthorizationCode)
                .build();
        settingsRepository.putSetting(userId, setting);

        StravaToken stravaToken = stravaClient.getStravaToken(clientAuthorizationCode);

        if (stravaToken != null) {

            storeTokenSettings(userId, stravaToken);

            settingsRepository.putSetting(userId, STRAVA_PROFILE_MEDIUM, stravaToken.getAthlete().getProfile_medium(), null);
            settingsRepository.putSetting(userId, STRAVA_LASTNAME, stravaToken.getAthlete().getLastname(), null);
            settingsRepository.putSetting(userId, STRAVA_FIRSTNAME, stravaToken.getAthlete().getFirstname(), null);
            settingsRepository.putSetting(userId, STRAVA_ATHLETE_ID, stravaToken.getAthlete().getId().toString(), null);

            return SyncedAccount.builder()
                    .image(stravaToken.getAthlete().getProfile_medium())
                    .syncedAccountId(stravaToken.getAthlete().getId())
                    .name(stravaToken.getAthlete().getFirstname() + " " + stravaToken.getAthlete().getLastname())
                    .build();
        } else {
            return null;
        }
    }

    private void storeTokenSettings(Integer userId, StravaToken stravaToken) {
        log.debug("Storing token update");
        settingsRepository.putSetting(userId, STRAVA_ACCESS_TOKEN, stravaToken.getAccess_token(), null);
        settingsRepository.putSetting(userId, STRAVA_EXPIRES_AT, stravaToken.getExpires_at().toString(), null);
        settingsRepository.putSetting(userId, STRAVA_REFRESH_TOKEN, stravaToken.getRefresh_token(), null);
    }

    public List<ListedActivityDto> getStravaActivitiesForDay(Integer userId, LocalDate date) {
        StravaToken token = getStravaToken(userId);

        log.error("Check is valid token");
        if (token == null) {
            log.error("Unable to get new token");
            return null;
        }
        if (!isStravaConnected(userId)) {
            log.error("Strava has not been setup for this user");
            return null;
        }

        log.debug("Token is valid");
        final List<ListedActivityDto> activitiesForDay = stravaClient.getActivitiesForDay(token.getAccess_token(), date);

        for (ListedActivityDto listedActivityDto : activitiesForDay) {
            final ActivityDetailsDto activityDetail = stravaClient.getActivityDetail(token.getAccess_token(), listedActivityDto.getId());
            log.debug(listedActivityDto.getStart_date_local() + " - " + listedActivityDto.getName() + " " + listedActivityDto.type + " " + listedActivityDto.getId());
            log.debug("Calorien: " + activityDetail.getCalories());
            listedActivityDto.setCalories(activityDetail.getCalories());
        }

        return activitiesForDay;

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

    public List<LogActivity> syncDay(List<LogActivity> dayActvities, Integer userId, LocalDate date) {
        List<LogActivity> newActivities = new ArrayList<>();
        if (isStravaConnected(userId)) {
            log.debug("Strava is connected. Syncing");

            // TODO: Only get details when activity has not been synced yet
            final List<ListedActivityDto> stravaActivitiesForDay = getStravaActivitiesForDay(userId, date);

            for (ListedActivityDto foundActivityFromSync : stravaActivitiesForDay) {
                log.debug("Checking to sync {}", foundActivityFromSync.getName());
                final long id = foundActivityFromSync.getId();
                final Optional<LogActivity> matchWithStoredActivity = dayActvities.stream().filter(a -> a.getSyncedId() == id).findAny();
                if (matchWithStoredActivity.isPresent()) {
                    final LogActivity logActivityFromSync = matchWithStoredActivity.get();
                    log.debug("Activity [{}]already known", logActivityFromSync.getName());
                } else {
                    log.debug("Activity [{}] not known");
                    final LogActivity newActivityFromSync = LogActivity.builder()
                            .day(Date.valueOf(date))
                            .name(foundActivityFromSync.getName())
                            .calories(foundActivityFromSync.getCalories())
                            .syncedId(foundActivityFromSync.getId())
                            .syncedWith("STRAVA")
                            .build();
                    final LogActivity logActivity = activityRepository.saveActivity(userId, newActivityFromSync);
                    newActivities.add(logActivity);
                }
            }
        }
        return newActivities;
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
