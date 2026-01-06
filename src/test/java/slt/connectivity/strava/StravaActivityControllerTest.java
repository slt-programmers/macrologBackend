package slt.connectivity.strava;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slt.config.StravaConfig;
import slt.connectivity.strava.dto.*;
import slt.database.ActivityRepository;
import slt.database.SettingsRepository;
import slt.database.entities.Activity;
import slt.database.entities.Setting;
import slt.connectivity.strava.dto.StravaSyncedAccountDto;
import slt.exceptions.ConnectivityException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StravaActivityControllerTest {

    private SettingsRepository settingsRepository;
    private ActivityRepository activityRepository;
    private StravaConfig stravaConfig;
    private StravaClient stravaClient;

    private StravaActivityService stravaActivityService;

    @BeforeEach
    public void setup() {
        settingsRepository = mock(SettingsRepository.class);
        activityRepository = mock(ActivityRepository.class);
        stravaConfig = mock(StravaConfig.class);
        stravaClient = mock(StravaClient.class);
        stravaActivityService = new StravaActivityService(settingsRepository, activityRepository, stravaConfig, stravaClient);
    }

    @Test
    void isStravaConnectedTrue() {
        when(settingsRepository.getLatestSetting(eq(1L), eq("STRAVA_ATHLETE_ID"))).thenReturn(Setting.builder().value("dad").build());
        final boolean stravaConnected = stravaActivityService.isStravaConnected(1L);
        Assertions.assertTrue(stravaConnected);
    }

    @Test
    void isStravaConnectedFalse() {
        when(settingsRepository.getLatestSetting(eq(1L), eq("STRAVA_ATHLETE_ID"))).thenReturn(null);
        final boolean stravaConnected = stravaActivityService.isStravaConnected(1L);
        Assertions.assertFalse(stravaConnected);
    }

    @Test
    void getStravaConnectivityWhenConnected() {
        mockSetting(1L, "STRAVA_ATHLETE_ID", "101");
        mockSetting(1L, "STRAVA_FIRSTNAME", "B");
        mockSetting(1L, "STRAVA_LASTNAME", "C");
        mockSetting(1L, "STRAVA_PROFILE", "D");

        when(activityRepository.countByUserIdAndSyncedWith(eq(1L), eq("STRAVA"))).thenReturn(1L);

        final var stravaConnectivity = stravaActivityService.getStravaConnectivity(1L);
        Assertions.assertEquals(101, stravaConnectivity.getSyncedAccountId());
        Assertions.assertEquals("B C", stravaConnectivity.getName());
        Assertions.assertEquals("D", stravaConnectivity.getImage());
        Assertions.assertEquals(1L, stravaConnectivity.getNumberActivitiesSynced());
        Assertions.assertNull(stravaConnectivity.getSyncedApplicationId());
    }

    @Test
    void getStravaConnectivityNotConnected() {
        mockSetting(1L, "STRAVA_ATHLETE_ID", null);

        when(activityRepository.countByUserIdAndSyncedWith(eq(1L), eq("STRAVA"))).thenReturn(1L);
        when(stravaConfig.getClientId()).thenReturn(201);

        final StravaSyncedAccountDto stravaConnectivity = stravaActivityService.getStravaConnectivity(1L);
        Assertions.assertNull(stravaConnectivity.getSyncedAccountId());
        Assertions.assertNull(stravaConnectivity.getName());
        Assertions.assertNull(stravaConnectivity.getImage());
        Assertions.assertNull(stravaConnectivity.getNumberActivitiesSynced());
        Assertions.assertEquals(201, stravaConnectivity.getSyncedApplicationId());
    }


    @Test
    void registerStravaConnectivityTokenOK() {
        settingsRepository.putSetting(any(Setting.class));
        settingsRepository.putSetting(any(Setting.class));

        Mockito.times(8);
        final StravaToken stravaToken = StravaToken.builder()
                .access_token("a")
                .expires_at(1L)
                .expires_in(2L)
                .athlete(StravaAthleteDto.builder().firstname("jan").lastname("patat").profile("profile").id(20L).build())
                .build();
        when(stravaClient.getStravaToken(eq("appelflap"))).thenReturn(Optional.ofNullable(stravaToken));
        when(activityRepository.countByUserIdAndSyncedWith(eq(1L), eq("STRAVA"))).thenReturn(1L);

        final StravaSyncedAccountDto stravaSyncedAccountDto = stravaActivityService.registerStravaConnectivity(
                1L, "appelflap");
        Assertions.assertEquals(1L, stravaSyncedAccountDto.getNumberActivitiesSynced());
        Assertions.assertEquals(20L, stravaSyncedAccountDto.getSyncedAccountId());
        Assertions.assertEquals("jan patat", stravaSyncedAccountDto.getName());
    }


    @Test
    void registerStravaConnectivityTokenNietOk() {
        settingsRepository.putSetting(any(Setting.class));

        Mockito.times(8);
        when(stravaClient.getStravaToken(eq("appelflap"))).thenReturn(Optional.empty());
        when(activityRepository.countByUserIdAndSyncedWith(eq(1L), eq("STRAVA"))).thenReturn(1L);

        Assertions.assertThrows(ConnectivityException.class, () -> stravaActivityService.registerStravaConnectivity(1L, "appelflap"));
    }

    @Test
    void unregisterStravaOK() {
        mockSetting(1L, "STRAVA_ATHLETE_ID", "12");
        mockSetting(1L, "STRAVA_ACCESS_TOKEN", "A");
        mockSetting(1L, "STRAVA_REFRESH_TOKEN", "B");

        final long toEpochSecond = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
        mockSetting(1L, "STRAVA_EXPIRES_AT", "" + toEpochSecond);
        when(stravaClient.unregister(any(StravaToken.class))).thenReturn(true);
        settingsRepository.deleteAllForUser(eq(1L), any(String.class));
        times(8);
        stravaActivityService.unregisterStrava(1L);
        verify(settingsRepository, times(8)).deleteAllForUser(eq(1L), any(String.class));
    }

    @Test
    void unregisterStravaNietOK() {
        // Voor ingelogd zijn:
        mockSetting(1L, "STRAVA_ATHLETE_ID", null);
        stravaActivityService.unregisterStrava(1L);
        verify(settingsRepository).getLatestSetting(eq(1L), any(String.class));
        verifyNoMoreInteractions(settingsRepository);
    }

    @Test
    void getStravaActivitiesForDay() {
        when(stravaClient.getActivitiesForDay(eq("a"), any(LocalDate.class))).thenReturn(new ArrayList<>());
        final var results = stravaActivityService.getStravaActivitiesForDay(StravaToken.builder().access_token("a").build(), LocalDate.now());
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void testExtraStravaActivitiesStravaConnectedNoForceNoStravaResults() {
        // Voor ingelogd zijn:
        mockSetting(1L, "STRAVA_ATHLETE_ID", "A");

        // Voor token:
        mockSetting(1L, "STRAVA_ATHLETE_ID", "12");
        mockSetting(1L, "STRAVA_ACCESS_TOKEN", "A");
        mockSetting(1L, "STRAVA_REFRESH_TOKEN", "B");
        final long toEpochSecond = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
        mockSetting(1L, "STRAVA_EXPIRES_AT", "" + toEpochSecond);

        when(stravaClient.getActivitiesForDay(eq("a"), any(LocalDate.class))).thenReturn(List.of());

        final var storedMacroLogActivities = List.of(Activity.builder().build());
        final var responseActivities = stravaActivityService.getExtraStravaActivities(
                storedMacroLogActivities, 1L, LocalDate.parse("2001-01-01"), false);

        Assertions.assertEquals(0, responseActivities.size());
    }

    @Test
    void testExtraStravaActivitiesStravaConnectedNoForceWithStravaResults() {
        // Voor ingelogd zijn:
        mockSetting(1L, "STRAVA_ATHLETE_ID", "A");

        // Voor token:
        mockSetting(1L, "STRAVA_ATHLETE_ID", "12");
        mockSetting(1L, "STRAVA_ACCESS_TOKEN", "A");
        mockSetting(1L, "STRAVA_REFRESH_TOKEN", "B");
        final long toEpochSecond = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
        mockSetting(1L, "STRAVA_EXPIRES_AT", "" + toEpochSecond);


        final var storedMacroLogActivities = List.of(Activity.builder().build());
        final var responseActivities = stravaActivityService.getExtraStravaActivities(storedMacroLogActivities, 1L, LocalDate.parse("2001-01-01"), false);

        Assertions.assertEquals(0, responseActivities.size());
    }

    @Test
    void testExtraStravaActivitiesStravaConnectedNoForceWithStravaResultsAlreadyKnown() {
        // Voor ingelogd zijn:
        mockSetting(1L, "STRAVA_ATHLETE_ID", "A");

        // Voor token:
        mockSetting(1L, "STRAVA_ATHLETE_ID", "12");
        mockSetting(1L, "STRAVA_ACCESS_TOKEN", "A");
        mockSetting(1L, "STRAVA_REFRESH_TOKEN", "B");
        final long toEpochSecond = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
        mockSetting(1L, "STRAVA_EXPIRES_AT", "" + toEpochSecond);

        when(stravaClient.getActivitiesForDay(eq("A"), any(LocalDate.class))).thenReturn(List.of(
                ListedActivityDto.builder().id(1L).build()
        ));

        when(stravaClient.getActivityDetail(eq("A"), eq(1L))).thenReturn(
                Optional.ofNullable(ActivityDetailsDto.builder().id(1L).build()));

        final var storedMacroLogActivities = List.of(
                Activity.builder().syncedId(1L).build());
        final var responseActivities = stravaActivityService.getExtraStravaActivities(storedMacroLogActivities, 1L, LocalDate.parse("2001-01-01"), false);

        Assertions.assertEquals(0, responseActivities.size());
    }

    @Test
    void testExtraStravaActivitiesStravaConnectedWithForceWithStravaResultsAlreadyKnownDeleted() {
        // Voor ingelogd zijn:
        mockSetting(1L, "STRAVA_ATHLETE_ID", "A");

        // Voor token:
        mockSetting(1L, "STRAVA_ATHLETE_ID", "12");
        mockSetting(1L, "STRAVA_ACCESS_TOKEN", "A");
        mockSetting(1L, "STRAVA_REFRESH_TOKEN", "B");
        final long toEpochSecond = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
        mockSetting(1L, "STRAVA_EXPIRES_AT", "" + toEpochSecond);

        when(stravaClient.getActivitiesForDay(eq("A"), any(LocalDate.class))).thenReturn(List.of(
                ListedActivityDto.builder().id(1L).build()
        ));

        when(stravaClient.getActivityDetail(eq("A"), eq(1L))).thenReturn(
                Optional.ofNullable(ActivityDetailsDto.builder().id(1L).build()));

        final var storedMacroLogActivities = List.of(
                Activity.builder().syncedId(1L).status("DELETED").build());
        final var responseActivities = stravaActivityService.getExtraStravaActivities(
                storedMacroLogActivities, 1L, LocalDate.parse("2001-01-01"), true);

        when(activityRepository.saveActivity(any(Activity.class))).thenReturn(Activity.builder().build());

        // dirty aanpassing van de parameter lijst naar niet meer gedelete
        Assertions.assertNull(storedMacroLogActivities.getFirst().getStatus());

        verify(activityRepository, times(1)).saveActivity(any());
        verify(stravaClient, times(1)).getActivitiesForDay(any(), any());
        verify(stravaClient, times(1)).getActivityDetail(any(), any());

        // already in result, not in extra results
        Assertions.assertEquals(0, responseActivities.size());
    }

    @Test
    void testExtraStravaActivitiesStravaConnectedWithNoForceButWebhookEnabledWithStravaResultsAlreadyKnownDeleted() {
        // Voor ingelogd zijn:
        mockSetting(1L, "STRAVA_ATHLETE_ID", "A");

        // Voor token:
        mockSetting(1L, "STRAVA_ATHLETE_ID", "12");
        mockSetting(1L, "STRAVA_ACCESS_TOKEN", "A");
        mockSetting(1L, "STRAVA_REFRESH_TOKEN", "B");
        final long toEpochSecond = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
        mockSetting(1L, "STRAVA_EXPIRES_AT", "" + toEpochSecond);

        when(stravaClient.getActivitiesForDay(eq("A"), any(LocalDate.class))).thenReturn(List.of(
                ListedActivityDto.builder().id(1L).build()
        ));

        when(stravaClient.getActivityDetail(eq("A"), eq(1L))).thenReturn(
                Optional.ofNullable(ActivityDetailsDto.builder().id(1L).build()));

        stravaActivityService.stravaWebhookSubscriptionId = 12;

        final var storedMacroLogActivities = List.of(
                Activity.builder().syncedId(1L).status("DELETED").build());
        final var responseActivities = stravaActivityService.getExtraStravaActivities(
                storedMacroLogActivities, 1L, LocalDate.parse("2001-01-01"), false);
        when(activityRepository.saveActivity(any(Activity.class))).thenReturn(Activity.builder().build());

        // De status is niet aangepast. Geen force geweest namelijk
        Assertions.assertEquals("DELETED", storedMacroLogActivities.getFirst().getStatus());

        // Geen conenctie naar strava, want de webhook staat aan. Alleen by force controleren we strava
        verify(activityRepository, times(0)).saveActivity(any());
        verify(stravaClient, times(0)).getActivitiesForDay(any(), any());
        verify(stravaClient, times(0)).getActivityDetail(any(), any());

        // already in result, not in extra results
        Assertions.assertEquals(0, responseActivities.size());
    }

    @Test
    public void testExpiredMechanisme() {
        // Voor ingelogd zijn:
        mockSetting(1L, "STRAVA_ATHLETE_ID", "A");

        // Voor token nu op expired:
        mockSetting(1L, "STRAVA_ATHLETE_ID", "12");
        mockSetting(1L, "STRAVA_ACCESS_TOKEN", "A");
        mockSetting(1L, "STRAVA_REFRESH_TOKEN", "B");
        final long toEpochSecond = LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.UTC);
        mockSetting(1L, "STRAVA_EXPIRES_AT", "" + toEpochSecond);

        when(stravaClient.refreshToken(eq("B"))).thenReturn(Optional.ofNullable(StravaToken.builder().expires_at(LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC)).build()));

        stravaActivityService.unregisterStrava(1L);

        verify(settingsRepository, times(3)).saveSetting(eq(1L), any(Setting.class));
    }

    @Test
    public void setupStravaWebhooksubscriptionUit() {
        when(stravaConfig.getClientId()).thenReturn(2);
        when(stravaConfig.getClientSecret()).thenReturn("a");
        when(stravaConfig.getVerifytoken()).thenReturn("uit");

        new StravaActivityService(settingsRepository, activityRepository, stravaConfig, stravaClient);

        verify(stravaConfig, times(3)).getVerifytoken();
        verifyNoMoreInteractions(stravaClient, settingsRepository, activityRepository, stravaConfig, stravaClient);
    }

    @Test
    public void setupStravaWebhooksubscriptionAan() {
        when(stravaConfig.getClientId()).thenReturn(2);
        when(stravaConfig.getClientSecret()).thenReturn("a");
        when(stravaConfig.getVerifytoken()).thenReturn("iets");
        when(stravaClient.viewWebhookSubscription(any(), any())).thenReturn(Optional.ofNullable(SubscriptionInformation.builder().build()));

        new StravaActivityService(settingsRepository, activityRepository, stravaConfig, stravaClient);

        verify(stravaConfig, times(3)).getVerifytoken();
        verify(stravaConfig, times(1)).getClientId();
        verify(stravaConfig, times(1)).getClientSecret();
        verify(stravaClient).viewWebhookSubscription(any(), any());
        verifyNoMoreInteractions(stravaClient, settingsRepository, activityRepository, stravaConfig);
    }

    @Test
    public void setupStravaWebhooksubscriptionAanNietGevonden() {
        when(stravaConfig.getClientId()).thenReturn(2);
        when(stravaConfig.getClientSecret()).thenReturn("a");
        when(stravaConfig.getVerifytoken()).thenReturn("iets");
        when(stravaClient.viewWebhookSubscription(any(), any())).thenReturn(Optional.empty());

        Assertions.assertThrows(ConnectivityException.class, () ->
                new StravaActivityService(settingsRepository, activityRepository, stravaConfig, stravaClient));

        verify(stravaConfig, times(3)).getVerifytoken();
        verify(stravaConfig, times(1)).getClientId();
        verify(stravaConfig, times(1)).getClientSecret();
        verify(stravaClient).viewWebhookSubscription(any(), any());
        verifyNoMoreInteractions(stravaClient, settingsRepository, activityRepository, stravaConfig);
    }

    @Test
    public void receiveWebhookWrongSubscription() {

        stravaActivityService.stravaWebhookSubscriptionId = 1;
        stravaActivityService.receiveWebhookEvent(WebhookEvent.builder().subscription_id(2).build());

        verify(stravaConfig, times(1)).getVerifytoken(); // Initialization of StravaActivityService
        verifyNoMoreInteractions(stravaClient, settingsRepository, activityRepository, stravaConfig);
    }

    @Test
    public void receiveWebhookAthleteNotFound() {

        stravaActivityService.stravaWebhookSubscriptionId = 1;
        when(settingsRepository.findByKeyValue(any(), any())).thenReturn(Optional.empty());

        stravaActivityService.receiveWebhookEvent(WebhookEvent.builder().subscription_id(1).owner_id(20L).build());

        verify(stravaConfig, times(1)).getVerifytoken(); // Initialization of StravaActivityService
        verify(settingsRepository).findByKeyValue(any(), any());

        verifyNoMoreInteractions(stravaClient, settingsRepository, activityRepository, stravaConfig);
    }

    @Test
    public void receiveWebhookNotActivity() {

        stravaActivityService.stravaWebhookSubscriptionId = 1;
        when(settingsRepository.findByKeyValue(any(), any())).thenReturn(Optional.of(Setting.builder().userId(123L).build()));

        // Voor token:
        mockSetting(123L, "STRAVA_ATHLETE_ID", "20");
        mockSetting(123L, "STRAVA_ACCESS_TOKEN", "A");
        mockSetting(123L, "STRAVA_REFRESH_TOKEN", "B");
        final long toEpochSecond = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
        mockSetting(123L, "STRAVA_EXPIRES_AT", "" + toEpochSecond);

        stravaActivityService.receiveWebhookEvent(WebhookEvent.builder().subscription_id(1).owner_id(20L).build());

        verify(stravaConfig, times(1)).getVerifytoken(); // Initialization of StravaActivityService
        verify(settingsRepository).findByKeyValue(any(), any());
        verify(settingsRepository, times(3)).getLatestSetting(any(), any());

        verifyNoMoreInteractions(stravaClient, settingsRepository, activityRepository, stravaConfig);
    }

    @Test
    public void receiveWebhookNoMatchingStravaActivity() {

        stravaActivityService.stravaWebhookSubscriptionId = 1;
        when(settingsRepository.findByKeyValue(any(), any())).thenReturn(Optional.of(Setting.builder().userId(123L).build()));

        // Voor token:
        mockSetting(123L, "STRAVA_ATHLETE_ID", "20");
        mockSetting(123L, "STRAVA_ACCESS_TOKEN", "A");
        mockSetting(123L, "STRAVA_REFRESH_TOKEN", "B");
        final long toEpochSecond = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
        mockSetting(123L, "STRAVA_EXPIRES_AT", "" + toEpochSecond);

        when(activityRepository.findByUserIdAndSyncIdAndSyncedWith(eq(123L), eq("STRAVA"), any())).thenReturn(Optional.empty());

        stravaActivityService.receiveWebhookEvent(WebhookEvent.builder().subscription_id(1).owner_id(20L).object_id(3L).object_type("activity").build());

        verify(stravaConfig, times(1)).getVerifytoken(); // Initialization of StravaActivityService
        verify(settingsRepository).findByKeyValue(any(), any());
        verify(settingsRepository, times(3)).getLatestSetting(any(), any());
        verify(activityRepository).findByUserIdAndSyncIdAndSyncedWith(any(), any(), anyLong());

        verifyNoMoreInteractions(stravaClient, settingsRepository, activityRepository, stravaConfig);
    }

    @Test
    public void receiveWebhookUpdateActivity() {

        stravaActivityService.stravaWebhookSubscriptionId = 1;
        when(settingsRepository.findByKeyValue(any(), any())).thenReturn(Optional.of(Setting.builder().userId(123L).build()));

        // Voor token:
        mockSetting(123L, "STRAVA_ATHLETE_ID", "20");
        mockSetting(123L, "STRAVA_ACCESS_TOKEN", "A");
        mockSetting(123L, "STRAVA_REFRESH_TOKEN", "B");
        final long toEpochSecond = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
        mockSetting(123L, "STRAVA_EXPIRES_AT", "" + toEpochSecond);

        when(activityRepository.findByUserIdAndSyncIdAndSyncedWith(eq(123L), eq("STRAVA"), any())).thenReturn(Optional.of(Activity.builder().build()));
        when(stravaClient.getActivityDetail(any(), eq(3L))).thenReturn(Optional.ofNullable(ActivityDetailsDto.builder().build()));

        stravaActivityService.receiveWebhookEvent(WebhookEvent.builder().subscription_id(1).owner_id(20L).object_id(3L).object_type("activity").aspect_type("create").build());

        verify(stravaConfig, times(1)).getVerifytoken(); // Initialization of StravaActivityService
        verify(settingsRepository).findByKeyValue(any(), any());
        verify(settingsRepository, times(3)).getLatestSetting(any(), any());
        verify(activityRepository).findByUserIdAndSyncIdAndSyncedWith(any(), any(), anyLong());
        verify(stravaClient).getActivityDetail(any(), any());
        verify(activityRepository).saveActivity(any());

        verifyNoMoreInteractions(stravaClient, settingsRepository, activityRepository, stravaConfig);
    }

    @Test
    public void receiveWebhookSaveNewActivity() {

        stravaActivityService.stravaWebhookSubscriptionId = 1;
        when(settingsRepository.findByKeyValue(any(), any())).thenReturn(Optional.of(Setting.builder().userId(123L).build()));

        // Voor token:
        mockSetting(123L, "STRAVA_ATHLETE_ID", "20");
        mockSetting(123L, "STRAVA_ACCESS_TOKEN", "A");
        mockSetting(123L, "STRAVA_REFRESH_TOKEN", "B");
        final long toEpochSecond = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
        mockSetting(123L, "STRAVA_EXPIRES_AT", "" + toEpochSecond);

        when(activityRepository.findByUserIdAndSyncIdAndSyncedWith(eq(123L), eq("STRAVA"), any())).thenReturn(Optional.empty());
        when(stravaClient.getActivityDetail(any(), eq(3L))).thenReturn(Optional.ofNullable(ActivityDetailsDto.builder().start_date("2018-02-16T14:52:54Z").build()));

        stravaActivityService.receiveWebhookEvent(WebhookEvent.builder().subscription_id(1).owner_id(20L).object_id(3L).object_type("activity").aspect_type("update").build());

        verify(stravaConfig, times(1)).getVerifytoken(); // Initialization of StravaActivityService
        verify(settingsRepository).findByKeyValue(any(), any());
        verify(settingsRepository, times(3)).getLatestSetting(any(), any());
        verify(activityRepository).findByUserIdAndSyncIdAndSyncedWith(any(), any(), anyLong());
        verify(stravaClient).getActivityDetail(any(), any());
        verify(activityRepository).saveActivity(any());

        verifyNoMoreInteractions(stravaClient, settingsRepository, activityRepository, stravaConfig);
    }

    @Test
    public void receiveWebhookDeleteKnownActivty() {

        stravaActivityService.stravaWebhookSubscriptionId = 1;
        when(settingsRepository.findByKeyValue(any(), any())).thenReturn(Optional.of(Setting.builder().userId(123L).build()));

        // Voor token:
        mockSetting(123L, "STRAVA_ATHLETE_ID", "20");
        mockSetting(123L, "STRAVA_ACCESS_TOKEN", "A");
        mockSetting(123L, "STRAVA_REFRESH_TOKEN", "B");
        final long toEpochSecond = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
        mockSetting(123L, "STRAVA_EXPIRES_AT", "" + toEpochSecond);

        when(activityRepository.findByUserIdAndSyncIdAndSyncedWith(eq(123L), eq("STRAVA"), any())).thenReturn(Optional.of(Activity.builder().id(300L).build()));

        stravaActivityService.receiveWebhookEvent(WebhookEvent.builder().subscription_id(1).owner_id(20L).object_id(3L).object_type("activity").aspect_type("delete").build());

        verify(stravaConfig, times(1)).getVerifytoken(); // Initialization of StravaActivityService
        verify(settingsRepository).findByKeyValue(any(), any());
        verify(settingsRepository, times(3)).getLatestSetting(any(), any());
        verify(activityRepository).findByUserIdAndSyncIdAndSyncedWith(any(), any(), anyLong());
        verify(activityRepository).deleteActivity(eq(123L), eq(300L));

        verifyNoMoreInteractions(stravaClient, settingsRepository, activityRepository, stravaConfig);
    }

    private void mockSetting(final Long userId, final String name, final String value) {
        if (value == null) {
            when(settingsRepository.getLatestSetting(eq(userId), eq(name))).thenReturn(null);
        } else {
            when(settingsRepository.getLatestSetting(eq(userId), eq(name))).thenReturn(Setting.builder().value(value).build());
        }
    }

}