import csl.database.SettingsRepository;
import csl.database.model.Setting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
class DatabaseExportTest {

    private SettingsRepository settingsRepo;

    @BeforeAll
    public void setUp() {
        settingsRepo = new SettingsRepository();
    }

    @Test
    public void exportSettingsDatabase() {
        List<Setting> allSettings = settingsRepo.getAllSettings();
        System.out.println(allSettings);

    }

}