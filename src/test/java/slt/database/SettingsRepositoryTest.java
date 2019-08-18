package slt.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import slt.database.entities.Setting;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SettingsRepositoryTest {

    @Mock
    SettingsCrudRepository settingsCrudRepository;

    @InjectMocks
    SettingsRepository settingsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void deleteAllForUser() {

        settingsRepository.deleteAllForUser(1, "A");
        verify(settingsCrudRepository).deleteAllByUserIdAndName(eq(1), eq("A"));
        verifyNoMoreInteractions(settingsCrudRepository);
    }

    @Test
    void getLatestSettingNotFound() {

        settingsRepository.getLatestSetting(1, "A");
        verify(settingsCrudRepository).findByUserIdAndNameOrderByDayDesc(eq(1), eq("A"));
        verifyNoMoreInteractions(settingsCrudRepository);
    }

    @Test
    void getLatestSettingFound() {

        when(settingsCrudRepository.findByUserIdAndNameOrderByDayDesc(eq(1), eq("A"))).thenReturn(Arrays.asList(Setting.builder().id(1).build()));
        final Setting setting = settingsRepository.getLatestSetting(1, "A");
        verify(settingsCrudRepository).findByUserIdAndNameOrderByDayDesc(eq(1), eq("A"));
        verifyNoMoreInteractions(settingsCrudRepository);

        assertThat(setting.getId()).isEqualTo(1);

    }

    @Test
    void findByKeyValue() {

        settingsRepository.findByKeyValue("A", "B");
        verify(settingsCrudRepository).findByNameAndValue(eq("A"), eq("B"));
        verifyNoMoreInteractions(settingsCrudRepository);
    }


}