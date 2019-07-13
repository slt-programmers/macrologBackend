package csl.servicetests;

import csl.database.model.Setting;
import csl.dto.UserSettingsDto;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import csl.servicetests.utils.AbstractApplicationIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SettingsServiceITest extends AbstractApplicationIntegrationTest {

    private Integer userId;

    @BeforeAll
    public void setUserContext() {
        synchronized (this) {
            if (this.userId == null) {
                log.debug("Creating test user for test " + this.getClass().getName());
                this.userId = createUser(this.getClass().getName());
            }
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Integer.valueOf(this.userId));
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }


    @Test
    public void testStoreAndGetSetting(){

        // Store setting with null date = NOW
        storeSetting("n1", "v1");

        // Store same setting with over a week
        LocalDate aWeekFromNow = LocalDate.now().plusDays(7);
        Setting settingWeekFromNow = Setting.builder().name("n1").value("v2").day(fromLocalDateToSQLDate(aWeekFromNow)).build();
        ResponseEntity responseEntityWeekFromNow = settingsService.storeSetting(settingWeekFromNow);
        assertThat(responseEntityWeekFromNow.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        // Store same setting with over a month
        LocalDate aMonthFromNow = LocalDate.now().plusMonths(1);
        Setting settingMonthFromNow = Setting.builder().name("n1").value("v3").day(fromLocalDateToSQLDate(aMonthFromNow)).build();
        ResponseEntity responseEntityMonthFromNow = settingsService.storeSetting(settingMonthFromNow);
        assertThat(responseEntityMonthFromNow.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        // Setting today exists
        ResponseEntity savedSetting = settingsService.getSetting("n1", null);
        assertThat(savedSetting.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(savedSetting.getBody().toString().equals("v1"));

        // Setting yesterday doesnt exist
        LocalDate yesterDay = LocalDate.now().minusDays(1);
        ResponseEntity yesterDayEntity = settingsService.getSetting("n1", yesterDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        assertThat(yesterDayEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(yesterDayEntity.getBody()).isNull();

        // Setting tomorrow does exist
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ResponseEntity tomorrowEntity = settingsService.getSetting("n1", tomorrow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        assertThat(tomorrowEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(tomorrowEntity.getBody().toString().equals("v1"));

        // Setting week from now is new value
        LocalDate weekFromNow = LocalDate.now().plusDays(7);
        ResponseEntity weekFromNowEntity = settingsService.getSetting("n1", weekFromNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        assertThat(weekFromNowEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(weekFromNowEntity.getBody().toString().equals("v2"));

        // Setting year from now is month value
        LocalDate yearFromNow = LocalDate.now().plusYears(1);
        ResponseEntity yearFromNowEntity = settingsService.getSetting("n1", yearFromNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        assertThat(yearFromNowEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(yearFromNowEntity.getBody().toString().equals("v3"));

        // request unsupported date format
        ResponseEntity unsupportedEntity = settingsService.getSetting("n1", "01-01-2020");
        assertThat(unsupportedEntity.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        // request strange date format
        ResponseEntity strangeEntity = settingsService.getSetting("n1", "20-20-2020");
        assertThat(strangeEntity.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void testGetUserSettings(){

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
        ResponseEntity userSettingEntity = settingsService.getUserSetting();
        assertThat(userSettingEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        return (UserSettingsDto) userSettingEntity.getBody();
    }

    private void storeSetting(String name, String value) {
        Setting setting = Setting.builder().name(name).value(value).build();
        ResponseEntity responseEntity = settingsService.storeSetting(setting);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }


    private java.sql.Date fromLocalDateToSQLDate(LocalDate localDate) {
        String dateForm = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return java.sql.Date.valueOf(dateForm);
    }
}
