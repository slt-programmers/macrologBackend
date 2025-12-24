package slt.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import slt.dto.SettingDto;
import slt.dto.UserSettingsDto;
import slt.exceptions.InvalidDateException;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.service.SettingsService;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

public class SettingsControllerTest {

    private SettingsService settingsService;
    private SettingsController controller;

    @BeforeEach
    void setup() {
        settingsService = mock(SettingsService.class);
        controller = new SettingsController(settingsService);
        final var userInfo = new UserInfo();
        userInfo.setUserId(1L);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    void getUserSettings() {
        final var dto = UserSettingsDto.builder()
                .name("n1")
                .age(45)
                .gender("M")
                .birthday(LocalDate.of(2012, 1, 1))
                .height(167)
                .currentWeight(70D)
                .activity(1.2D)
                .goalCarbs(123)
                .goalFat(56)
                .goalProtein(89)
                .build();
        when(settingsService.getUserSettingsDto(1L)).thenReturn(dto);
        final var result = controller.getUserSettings();
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(dto, result.getBody());
    }

    @Test
    void getSettingWithoutDate() {
        when(settingsService.getSetting(1L, "age", null)).thenReturn("66");
        final var result = controller.getSetting("age", null);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals("66", result.getBody());
    }

    @Test
    void getSettingWithDate() {
        when(settingsService.getSetting(1L, "age", "2025-01-01")).thenReturn("66");
        final var result = controller.getSetting("age", "2025-01-01");
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals("66", result.getBody());
    }

    @Test
    void getSettingWithInvalidDate() {
        Assertions.assertThrows(InvalidDateException.class,
                () -> controller.getSetting("age", "01-01-2025"));
    }

    @Test
    void putSetting() {
        final var dto = SettingDto.builder().build();
        final var result = controller.putSetting(dto);
        verify(settingsService).putSetting(1L, dto);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}
