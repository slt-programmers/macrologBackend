import csl.database.LogEntryRepository;
import csl.database.SettingsRepository;
import csl.database.model.Setting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RunWith(JUnit4.class)
class DatabaseExportTest {

    private SettingsRepository settingsRepo;
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseExportTest.class);

    @BeforeAll
    public void setUp() {
        settingsRepo = new SettingsRepository();
    }

    @Test
    public void exportSettingsDatabase() {
        Integer userId = -1;
        List<Setting> allSettings = settingsRepo.getAllSettings(userId);
        LOGGER.debug("all Settings:" + allSettings);

    }

}