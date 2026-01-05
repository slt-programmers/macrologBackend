package slt.rest;

import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import slt.connectivity.strava.StravaActivityService;
import slt.dto.SettingDto;
import slt.connectivity.strava.dto.StravaSyncedAccountDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import static org.mockito.Mockito.*;

class ConnectivityControllerTest {

    private StravaActivityService stravaActivityService;
    private ConnectivityController controller;

    @BeforeEach
    void setup() {
        stravaActivityService = mock(StravaActivityService.class);
        controller = new ConnectivityController(stravaActivityService);
        final var userInfo = new UserInfo();
        userInfo.setUserId(-1L);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    void getConnectivitySettingStrava() {
        final var syncedAccount = StravaSyncedAccountDto.builder().build();
        when(stravaActivityService.getStravaConnectivity(-1L)).thenReturn(syncedAccount);
        final var result = controller.getConnectivitySetting("STRAVA");
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(syncedAccount, result.getBody());
    }

    @Test
    void getConnectivitySettingNotImplemented() {
        Assertions.assertThrows(NotImplementedException.class,
                () -> controller.getConnectivitySetting("NOT_STRAVA"));
    }

    @Test
    void postConnectivitySettingStrava() {
        final var dto = SettingDto.builder()
                .name("code")
                .value("somevalue")
                .build();
        final var syncedAccount = StravaSyncedAccountDto.builder().build();
        when(stravaActivityService.registerStravaConnectivity(-1L, "somevalue")).thenReturn(syncedAccount);
        final var result = controller.postConnectivitySetting("STRAVA",dto);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(syncedAccount, result.getBody());
    }

    @Test
    void postConnectivitySettingNotImplemented() {
        Assertions.assertThrows(NotImplementedException.class,
                () -> controller.postConnectivitySetting("NOT_STRAVA", SettingDto.builder().build()));
    }

    @Test
    void deleteConnectivitySettingStrava() {
        final var result = controller.deleteConnectivitySetting("STRAVA");
        verify(stravaActivityService).unregisterStrava(-1L);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void deleteConnectivitySettingNotImplemented() {
        Assertions.assertThrows(NotImplementedException.class,
                () -> controller.deleteConnectivitySetting("NOT_STRAVA"));
    }
}