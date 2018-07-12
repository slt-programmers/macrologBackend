package csl;

import csl.database.*;
import csl.dto.AddFoodRequest;
import csl.dto.AddLogEntryRequest;
import csl.dto.Portion;
import csl.enums.MeasurementUnit;
import csl.rest.FoodService;
import csl.rest.LogsService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.net.URISyntaxException;
import java.sql.*;
import java.util.Arrays;
import java.util.List;


@SpringBootApplication
@EnableAutoConfiguration
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);

        //DEV/TEST purposes
        boolean fillTablesOnStartup = true;
        boolean clearTablesOnStartup = true;

        if (clearTablesOnStartup) {
            deleteTables();
        }

        boolean tableExists = isDatabaseSetUp();
        if (!tableExists) {
            createTables();
        } else {
            LOGGER.info("Tables already set up");
        }

        if (fillTablesOnStartup) {
            createTestdata();
        }

        //Application is now running

    }

    private static void createTestdata() throws URISyntaxException {
        LOGGER.info("Filling tables with testdata");
        FoodService foodService = new FoodService();
        FoodRepository foodRepository = new FoodRepository();
        PortionRepository portionRepository = new PortionRepository();
        LogsService logService = new LogsService();

        String BROOD = "Brood: Goudeerlijk bus fijn volkoren Jumbo";
        String BROOD_SNEE = "snee";

        AddFoodRequest addFoodRequest = createAddFoodRequest(BROOD,
                MeasurementUnit.GRAMS,
                null, null,
                1.1,
                2.0,
                38.0,
                Arrays.asList(createPortion(BROOD_SNEE, null, 30.0)));
        foodService.addFood(addFoodRequest);
        Long BROOD_ID = foodRepository.getFood(BROOD).getId();


        String MUESLI = "Jordans Muesli de luxe";
        String MUESLI_SCHAAL = "schaal Muesli";
        AddFoodRequest addFoodRequest1 = createAddFoodRequest(MUESLI, MeasurementUnit.GRAMS, null, null, 9.4, 9.2, 59.3, Arrays.asList(createPortion(MUESLI_SCHAAL, null, 80)));
        foodService.addFood(addFoodRequest1);
        Long MUESLI_FOOD_ID = foodRepository.getFood(MUESLI).getId();

        String YOGHURT_VOL_JUMBO = "Yoghurt vol Jumbo";
        String YOGHURT_VOL_SCHAAL = "schaal Yoghurt Vol";
        AddFoodRequest addFoodRequest2 = createAddFoodRequest(YOGHURT_VOL_JUMBO, MeasurementUnit.GRAMS, null,null,4.1, 3.1, 4.2,Arrays.asList(createPortion(YOGHURT_VOL_SCHAAL, null,200)));
        foodService.addFood(addFoodRequest2);
        Long YOGHURT_FOOD_ID = foodRepository.getFood(YOGHURT_VOL_JUMBO).getId();

        String CALVE_PINDAKAAS = "Calvé pindakaas";
        String CALVE_PINDAKAAS_BELEG_2 = "2 boterhammen pindakaas beleg";
        AddFoodRequest addFoodRequest3 = createAddFoodRequest(CALVE_PINDAKAAS, MeasurementUnit.GRAMS, null,null,20, 55, 15,Arrays.asList(createPortion(CALVE_PINDAKAAS_BELEG_2, null,20)));
        foodService.addFood(addFoodRequest3);
        Long CALVE_PINDAKAAS_FOOD_ID = foodRepository.getFood(CALVE_PINDAKAAS).getId();

        String EI = "Ei hardgekookt";
        String EI_STUK = "ei";
        AddFoodRequest addFoodRequest4 = createAddFoodRequest(EI, MeasurementUnit.UNIT, EI_STUK, 58.0, 12.3, 9.1, 0.2,null);
        foodService.addFood(addFoodRequest4);
//        aliasRequest = createPortion(EI_STUK, 58.0, "gram");
        Long EI_ID = foodRepository.getFood(EI).getId();
//        foodService.addPortion(EI_ID, aliasRequest);
//
        AddLogEntryRequest logEntry1 = new AddLogEntryRequest();
        logEntry1.setDay(new DateTime(2018, 6, 21, 7, 0).toDate());
        logEntry1.setFoodId(MUESLI_FOOD_ID);
        logEntry1.setPortionId(portionRepository.getPortion(MUESLI_FOOD_ID, MUESLI_SCHAAL).getId());
        logEntry1.setMultiplier(1.0);
        logEntry1.setMeal("BREAKFAST");
        logService.storeLogEntry(logEntry1);

        AddLogEntryRequest logEntry2 = new AddLogEntryRequest();
        logEntry2.setDay(new DateTime(2018, 6, 21, 7, 0).toDate());
        logEntry2.setFoodId(YOGHURT_FOOD_ID);
        logEntry2.setPortionId(portionRepository.getPortion(YOGHURT_FOOD_ID, YOGHURT_VOL_SCHAAL).getId());
        logEntry2.setMultiplier(1.0);
        logEntry2.setMeal("BREAKFAST");
        logService.storeLogEntry(logEntry2);

        AddLogEntryRequest logEntry3 = new AddLogEntryRequest();
        logEntry3.setDay(new DateTime(2018, 6, 21, 7, 0).toDate());
        logEntry3.setFoodId(EI_ID);
        logEntry3.setMultiplier(4.0);
        logEntry3.setMeal("LUNCH");
        logService.storeLogEntry(logEntry3);

        AddLogEntryRequest logEntry4 = new AddLogEntryRequest();
        logEntry4.setDay(new DateTime(2018, 6, 21, 7, 0).toDate());
        logEntry4.setFoodId(BROOD_ID);
        logEntry4.setPortionId(portionRepository.getPortion(BROOD_ID, BROOD_SNEE).getId());
        logEntry4.setMultiplier(4.0);
        logEntry4.setMeal("LUNCH");
        logService.storeLogEntry(logEntry4);

        AddLogEntryRequest logEntry5 = new AddLogEntryRequest();
        logEntry5.setDay(new DateTime(2018, 6, 21, 7, 0).toDate());
        logEntry5.setFoodId(CALVE_PINDAKAAS_FOOD_ID);
        logEntry5.setPortionId(portionRepository.getPortion(CALVE_PINDAKAAS_FOOD_ID, CALVE_PINDAKAAS_BELEG_2).getId());
        logEntry5.setMultiplier(2.0);
        logEntry5.setMeal("DINNER");
        logService.storeLogEntry(logEntry5);
    }

    private static csl.dto.Portion createPortion(String description, Double unitMultiplier, double grams) {
        csl.dto.Portion portion = new csl.dto.Portion();
        portion.setDescription(description);
        portion.setUnitMultiplier(unitMultiplier);
        portion.setGrams(grams);
        return portion;
    }

//    private static AddFoodRequest createAddFoodRequest(String name, MeasurementUnit typeUnit, double protein, double fat, double carbs) {
//        return createAddFoodRequest(name, typeUnit, null, 0.0, protein, fat, carbs, null);
//    }

    private static AddFoodRequest createAddFoodRequest(String name,
                                                       MeasurementUnit typeUnit,
                                                       String unitName,
                                                       Double unitGrams,
                                                       double protein,
                                                       double fat,
                                                       double carbs,
                                                       List<Portion> portions) {
        AddFoodRequest foodRequest = new AddFoodRequest();
        foodRequest.setName(name);
        foodRequest.setMeasurementUnit(typeUnit);
        foodRequest.setUnitName(unitName);
        foodRequest.setUnitGrams(unitGrams);
        foodRequest.setProtein(protein);
        foodRequest.setFat(fat);
        foodRequest.setCarbs(carbs);
        foodRequest.setPortions(portions);
        return foodRequest;
    }

    private static void deleteTables() {
        LOGGER.info("Deleting tables on startup");
        try (Connection connection = DatabaseHelper.getInstance().getConnection();
             CallableStatement deleteOldFoodAlias = connection.prepareCall("DROP TABLE IF EXISTS FOOD_ALIAS");
             CallableStatement deleteLogEntry = connection.prepareCall(LogEntryRepository.TABLE_DELETE);
             CallableStatement deletePortion = connection.prepareCall(PortionRepository.TABLE_DELETE);
             CallableStatement deleteFood = connection.prepareCall(FoodRepository.TABLE_DELETE)) {
            CallableStatement deleteSettings = connection.prepareCall(SettingsRepository.TABLE_DELETE);

            LOGGER.info("Deleting old food alias");
            deleteOldFoodAlias.execute();
            LOGGER.info("Deleting deleteLogEntry");
            deleteLogEntry.execute();
            LOGGER.info("Deleting deletePortion");
            deletePortion.execute();
            LOGGER.info("Deleting deleteFood");
            deleteFood.execute();
            LOGGER.info("Deleting deleteLogEntry");
            deleteSettings.execute();

        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private static void createTables() {
        LOGGER.info("Creating tables on startup");
        try (Connection connection = DatabaseHelper.getInstance().getConnection();
             CallableStatement settingsTableCreate = connection.prepareCall(SettingsRepository.TABLE_CREATE);
             CallableStatement footTableCreate = connection.prepareCall(FoodRepository.TABLE_CREATE);
             CallableStatement foodPortionCreate = connection.prepareCall(PortionRepository.TABLE_CREATE);
             CallableStatement logEntryTableCreate = connection.prepareCall(LogEntryRepository.TABLE_CREATE)) {

            settingsTableCreate.execute();
            footTableCreate.execute();
            foodPortionCreate.execute();
            logEntryTableCreate.execute();

            LOGGER.info("Tables created succesfully");
        } catch (SQLException e) {
            LOGGER.error("Failed to create tables");
            LOGGER.error(e.getMessage());
        }
    }

    private static boolean isDatabaseSetUp() throws SQLException {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));
        String checkUrl = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES where" +
                " table_name = '" + SettingsRepository.TABLE_NAME + "' or " +
                " table_name = '" + FoodRepository.TABLE_NAME + "' or " +
                " table_name = '" + PortionRepository.TABLE_NAME + "' or " +
                " table_name = '" + LogEntryRepository.TABLE_NAME + "'";
        List<String> results = template.query(checkUrl, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("TABLE_NAME");
            }
        });
        return results.size() == 3;
    }
}