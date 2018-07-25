import csl.dto.DayMacro;
import csl.dto.Macro;
import csl.rest.LogEntryService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class LogEntryServiceTest {


    private LogEntryService myService = new LogEntryService();
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseExportTest.class);

    @Test
    public void testMe() {
        ResponseEntity macrosFromPeriod = myService.getMacrosFromPeriod("1", "2");

        List<DayMacro> macrosPerDay = (List<DayMacro>) macrosFromPeriod.getBody();
        for (DayMacro dayMacro : macrosPerDay) {
            LOGGER.debug(dayMacro.getDay() + " - " + dayMacro.getMacro().getProtein() + " " + dayMacro.getMacro().getFat() + " " + dayMacro.getMacro().getCarbs());
        }

    }

}
