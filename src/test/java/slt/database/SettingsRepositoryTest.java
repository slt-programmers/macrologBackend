package slt.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slt.database.entities.Setting;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SettingsRepositoryTest {

    private SettingsCrudRepository settingsCrudRepository;
    private SettingsRepository settingsRepository;

    @BeforeEach
    void setUp() {
        settingsCrudRepository = mock(SettingsCrudRepository.class);
        settingsRepository = new SettingsRepository(settingsCrudRepository);
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
        when(settingsCrudRepository.findByUserIdAndNameOrderByDayDesc(eq(1L), eq("A"))).thenReturn(
                List.of(Setting.builder().id(1L).build()));
        final Setting setting = settingsRepository.getLatestSetting(1L, "A");
        verify(settingsCrudRepository).findByUserIdAndNameOrderByDayDesc(eq(1L), eq("A"));
        verifyNoMoreInteractions(settingsCrudRepository);
        Assertions.assertEquals(1, setting.getId());
    }

    @Test
    void findByKeyValue() {
        settingsRepository.findByKeyValue("A", "B");
        verify(settingsCrudRepository).findByNameAndValue(eq("A"), eq("B"));
        verifyNoMoreInteractions(settingsCrudRepository);
    }

}