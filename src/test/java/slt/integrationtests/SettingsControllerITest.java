package slt.integrationtests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpStatus;
import slt.dto.SettingDto;
import slt.dto.UserSettingsDto;
import slt.dto.WeightDto;
import slt.exceptions.InvalidDateException;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.integrationtests.utils.AbstractApplicationIntegrationTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SettingsControllerITest extends AbstractApplicationIntegrationTest {

    private Long userId;

    @BeforeAll
    public void setUserContext() {
        synchronized (this) {
            if (this.userId == null) {
                log.debug("Creating test user for test {}", this.getClass().getName());
                this.userId = createUser(this.getClass().getName());
            }
        }
        final var userInfo = UserInfo.builder().userId(this.userId).build();
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }


    @Test
    public void testStoreAndGetSetting() {
        // Store setting with null date = NOW
        saveSetting("n1", "v1");

        // Store same setting with over a week
        final var aWeekFromNow = LocalDate.now().plusDays(7);
        final var settingDtoWeekFromNow = SettingDto.builder().name("n1").value("v2").day(aWeekFromNow).build();
        final var responseEntityWeekFromNow = settingsController.putSetting(settingDtoWeekFromNow);
        Assertions.assertEquals(HttpStatus.OK, responseEntityWeekFromNow.getStatusCode());

        // Store same setting with over a month
        final var aMonthFromNow = LocalDate.now().plusMonths(1);
        final var settingDtoMonthFromNow = SettingDto.builder().name("n1").value("v3").day(aMonthFromNow).build();
        final var responseEntityMonthFromNow = settingsController.putSetting(settingDtoMonthFromNow);
        Assertions.assertEquals(HttpStatus.OK, responseEntityMonthFromNow.getStatusCode());

        // SettingDto today exists
        final var savedSetting = settingsController.getSetting("n1", null);
        Assertions.assertEquals(HttpStatus.OK, savedSetting.getStatusCode());
        Assertions.assertEquals("v3", savedSetting.getBody());

        // SettingDto yesterday refer to next valid setting
        final var yesterDay = LocalDate.now().minusDays(1);
        final var yesterDayEntity = settingsController.getSetting("n1", yesterDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        Assertions.assertEquals(HttpStatus.OK, yesterDayEntity.getStatusCode());
        Assertions.assertEquals("v1", yesterDayEntity.getBody());

        // SettingDto tomorrow refer to previously valid setting
        final var tomorrow = LocalDate.now().plusDays(1);
        final var tomorrowEntity = settingsController.getSetting("n1", tomorrow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        Assertions.assertEquals(HttpStatus.OK, tomorrowEntity.getStatusCode());
        Assertions.assertEquals("v1", tomorrowEntity.getBody());

        // SettingDto week from now is new value
        final var weekFromNow = LocalDate.now().plusDays(7);
        final var weekFromNowEntity = settingsController.getSetting("n1", weekFromNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        Assertions.assertEquals(HttpStatus.OK, weekFromNowEntity.getStatusCode());
        Assertions.assertEquals("v2", weekFromNowEntity.getBody());

        // SettingDto year from now is month value
        final var yearFromNow = LocalDate.now().plusYears(1);
        final var yearFromNowEntity = settingsController.getSetting("n1", yearFromNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        Assertions.assertEquals(HttpStatus.OK, yearFromNowEntity.getStatusCode());
        Assertions.assertEquals("v3", yearFromNowEntity.getBody());

        // request unsupported date format
        Assertions.assertThrows(InvalidDateException.class, () -> settingsController.getSetting("n1", "01-01-2020"));
        Assertions.assertThrows(InvalidDateException.class, () -> settingsController.getSetting("n1", "20-20-2020"));
    }

    @Test
    public void testGetUserSettings() {
        // Initieel leeg:
        final var userSettingsDto = getUserSettingsDto();

        Assertions.assertNull(userSettingsDto.getName());
        Assertions.assertNull(userSettingsDto.getGender());
        Assertions.assertNull(userSettingsDto.getAge());
        Assertions.assertNull(userSettingsDto.getBirthday());
        Assertions.assertNull(userSettingsDto.getHeight());
        Assertions.assertNull(userSettingsDto.getActivity());
        Assertions.assertNull(userSettingsDto.getCurrentWeight());
        Assertions.assertNull(userSettingsDto.getGoalProtein());
        Assertions.assertNull(userSettingsDto.getGoalFat());
        Assertions.assertNull(userSettingsDto.getGoalCarbs());

        // Vullen met data:
        saveNameSetting("t1", LocalDate.of(2025, 6, 1));
        saveNameSetting("previous", LocalDate.of(2025, 1, 1));
        saveSetting("gender", "m");
        saveSetting("age", "99");
        saveSetting("birthday", "18-06-1978");
        saveSetting("height", "187");
        saveSetting("activity", "1");
        saveSetting("goalProtein", "50");
        saveSetting("goalFat", "60");
        saveSetting("goalCarbs", "70");
        weightController.postWeight(WeightDto.builder().weight(76D).day(LocalDate.of(2025, 1, 1)).build());
        weightController.postWeight(WeightDto.builder().weight(75D).day(LocalDate.of(2024, 1, 1)).build());

        final var newSettingsDto = getUserSettingsDto();
        Assertions.assertEquals("t1", newSettingsDto.getName());
        Assertions.assertEquals("m", newSettingsDto.getGender());
        Assertions.assertEquals(99, newSettingsDto.getAge());
        Assertions.assertEquals(LocalDate.parse("1978-06-18"), newSettingsDto.getBirthday());
        Assertions.assertEquals(187, newSettingsDto.getHeight());
        Assertions.assertEquals(1, newSettingsDto.getActivity());
        Assertions.assertEquals(76D, newSettingsDto.getCurrentWeight());
        Assertions.assertEquals(50, newSettingsDto.getGoalProtein());
        Assertions.assertEquals(60, newSettingsDto.getGoalFat());
        Assertions.assertEquals(70, newSettingsDto.getGoalCarbs());

    }

    private UserSettingsDto getUserSettingsDto() {
        final var userSettingEntity = settingsController.getUserSettings();
        Assertions.assertEquals(HttpStatus.OK, userSettingEntity.getStatusCode());
        return userSettingEntity.getBody();
    }

}
