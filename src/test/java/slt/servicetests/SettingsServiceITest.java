package slt.servicetests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import slt.dto.SettingDto;
import slt.dto.UserSettingsDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.servicetests.utils.AbstractApplicationIntegrationTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SettingsServiceITest extends AbstractApplicationIntegrationTest {

    private Long userId;

    @BeforeAll
    public void setUserContext() {
        synchronized (this) {
            if (this.userId == null) {
                log.debug("Creating test user for test " + this.getClass().getName());
                this.userId = createUser(this.getClass().getName());
            }
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(this.userId);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }


    @Test
    public void testStoreAndGetSetting() {

        // Store setting with null date = NOW
        storeSetting("n1", "v1");

        // Store same setting with over a week
        LocalDate aWeekFromNow = LocalDate.now().plusDays(7);
        SettingDto settingDtoWeekFromNow = SettingDto.builder().name("n1").value("v2").day(fromLocalDateToSQLDate(aWeekFromNow)).build();
        ResponseEntity<Void> responseEntityWeekFromNow = settingsService.storeSetting(settingDtoWeekFromNow);
        assertThat(responseEntityWeekFromNow.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Store same setting with over a month
        LocalDate aMonthFromNow = LocalDate.now().plusMonths(1);
        SettingDto settingDtoMonthFromNow = SettingDto.builder().name("n1").value("v3").day(fromLocalDateToSQLDate(aMonthFromNow)).build();
        ResponseEntity<Void> responseEntityMonthFromNow = settingsService.storeSetting(settingDtoMonthFromNow);
        assertThat(responseEntityMonthFromNow.getStatusCode()).isEqualTo(HttpStatus.OK);

        // SettingDto today exists
        ResponseEntity<String> savedSetting = settingsService.getSetting("n1", null);
        assertThat(savedSetting.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(savedSetting.getBody()).isEqualTo("v3");

        // SettingDto yesterday refer to next valid setting
        LocalDate yesterDay = LocalDate.now().minusDays(1);
        ResponseEntity<String> yesterDayEntity = settingsService.getSetting("n1", yesterDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        assertThat(yesterDayEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(savedSetting.getBody()).isEqualTo("v3");

        // SettingDto tomorrow refer to previously valid setting
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ResponseEntity<String> tomorrowEntity = settingsService.getSetting("n1", tomorrow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        assertThat(tomorrowEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(savedSetting.getBody()).isEqualTo("v3");

        // SettingDto week from now is new value
        LocalDate weekFromNow = LocalDate.now().plusDays(7);
        ResponseEntity<String> weekFromNowEntity = settingsService.getSetting("n1", weekFromNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        assertThat(weekFromNowEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(weekFromNowEntity.getBody()).isEqualTo("v2");

        // SettingDto year from now is month value
        LocalDate yearFromNow = LocalDate.now().plusYears(1);
        ResponseEntity<String> yearFromNowEntity = settingsService.getSetting("n1", yearFromNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        assertThat(yearFromNowEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(yearFromNowEntity.getBody()).isEqualTo("v3");

        // request unsupported date format
        ResponseEntity<String> unsupportedEntity = settingsService.getSetting("n1", "01-01-2020");
        assertThat(unsupportedEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // request strange date format
        ResponseEntity<String> strangeEntity = settingsService.getSetting("n1", "20-20-2020");
        assertThat(strangeEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testGetUserSettings() {

        // Initieel leeg:
        UserSettingsDto userSettingsDto = getUserSettingsDto();

        assertThat(userSettingsDto.getName()).isNull();
        assertThat(userSettingsDto.getGender()).isNull();
        assertThat(userSettingsDto.getAge()).isNull();
        assertThat(userSettingsDto.getBirthday()).isNull();
        assertThat(userSettingsDto.getHeight()).isNull();
        assertThat(userSettingsDto.getActivity()).isNull();
        assertThat(userSettingsDto.getGoalProtein()).isNull();
        assertThat(userSettingsDto.getGoalFat()).isNull();
        assertThat(userSettingsDto.getGoalCarbs()).isNull();

        // Vullen met data:
        storeSetting("name", "t1");
        storeSetting("gender", "m");
        storeSetting("age", "99");
        storeSetting("birthday", "18-06-1978");
        storeSetting("height", "187");
        storeSetting("activity", "1");
        storeSetting("goalProtein", "50");
        storeSetting("goalFat", "60");
        storeSetting("goalCarbs", "70");

        UserSettingsDto newSettingsDto = getUserSettingsDto();
        assertThat(newSettingsDto.getName()).isEqualTo("t1");
        assertThat(newSettingsDto.getGender()).isEqualTo("m");
        assertThat(newSettingsDto.getAge()).isEqualTo(99);
        assertThat(newSettingsDto.getBirthday()).isEqualTo(LocalDate.parse("1978-06-18"));
        assertThat(newSettingsDto.getHeight()).isEqualTo(187);
        assertThat(newSettingsDto.getActivity()).isEqualTo(1);
        assertThat(newSettingsDto.getGoalProtein()).isEqualTo(50);
        assertThat(newSettingsDto.getGoalFat()).isEqualTo(60);
        assertThat(newSettingsDto.getGoalCarbs()).isEqualTo(70);

    }

    private UserSettingsDto getUserSettingsDto() {
        ResponseEntity<UserSettingsDto> userSettingEntity = settingsService.getUserSetting();
        assertThat(userSettingEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        return userSettingEntity.getBody();
    }

    private java.sql.Date fromLocalDateToSQLDate(LocalDate localDate) {
        String dateForm = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return java.sql.Date.valueOf(dateForm);
    }
}
