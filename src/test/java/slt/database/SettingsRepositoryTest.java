package slt.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import slt.database.entities.Setting;

import java.util.Collections;

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

        settingsRepository.deleteAllForUser(1L, "A");
        verify(settingsCrudRepository).deleteAllByUserIdAndName(eq(1L), eq("A"));
        verifyNoMoreInteractions(settingsCrudRepository);
    }

    @Test
    void getLatestSettingNotFound() {

        settingsRepository.getLatestSetting(1L, "A");
        verify(settingsCrudRepository).findByUserIdAndNameOrderByDayDesc(eq(1L), eq("A"));
        verifyNoMoreInteractions(settingsCrudRepository);
    }

    @Test
    void getLatestSettingFound() {

        when(settingsCrudRepository.findByUserIdAndNameOrderByDayDesc(eq(1L), eq("A"))).thenReturn(Collections.singletonList(Setting.builder().id(1L).build()));
        final Setting setting = settingsRepository.getLatestSetting(1L, "A");
        verify(settingsCrudRepository).findByUserIdAndNameOrderByDayDesc(eq(1L), eq("A"));
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