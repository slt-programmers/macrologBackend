package csl;

import csl.database.*;
import csl.rest.FoodService;
import csl.rest.LogsService;
import jdk.nashorn.internal.codegen.CompilerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.net.URISyntaxException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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

//        String BROOD = "Brood: Goudeerlijk bus fijn volkoren Jumbo";
//        String BROOD_SNEE = "snee";
//        csl.dto.Portion portion = createPortion(BROOD_SNEE, null,30.0);
//        foodService.addFood(createAddFoodRequest(BROOD, MeasurementUnit.UNIT, BROOD_SNEE, 0.0, 1.1, 2.0, 38.0, new ArrayList<>().add(portion)));
//        Long BROOD_ID = foodRepository.getFood(BROOD).getId();
////        foodService.addPortion(BROOD_ID, aliasRequest);
//
//        String MUESLI = "Jordans Muesli de luxe";
//        String MUESLI_SCHAAL = "schaal Muesli";
//        foodService.addFood(createAddFoodRequest(MUESLI, MeasurementUnit.GRAMS, 9.4, 9.2, 59.3));
//        Long MUESLI_FOOD_ID = foodRepository.getFood(MUESLI).getId();
//        aliasRequest = createPortion(MUESLI_SCHAAL, 80, "gram");
//        foodService.addPortion(MUESLI_FOOD_ID, aliasRequest);
//
//        String YOGHURT_VOL_JUMBO = "Yoghurt vol Jumbo";
//        String YOGHURT_VOL_SCHAAL = "schaal Yoghurt Vol";
//        foodService.addFood(createAddFoodRequest(YOGHURT_VOL_JUMBO, MeasurementUnit.GRAMS, 4.1, 3.1, 4.2));
//        Long YOGHURT_FOOD_ID = foodRepository.getFood(YOGHURT_VOL_JUMBO).getId();
//        aliasRequest = createPortion(YOGHURT_VOL_SCHAAL, 200, "gram");
//        foodService.addPortion(YOGHURT_FOOD_ID, aliasRequest);
//
//        String CALVE_PINDAKAAS = "Calvé pindakaas";
//        String CALVE_PINDAKAAS_BELEG_2 = "2 boterhammen pindakaas beleg";
//        foodService.addFood(createAddFoodRequest(CALVE_PINDAKAAS, MeasurementUnit.GRAMS, 20, 55, 15));
//        Long CALVE_PINDAKAAS_FOOD_ID = foodRepository.getFood(CALVE_PINDAKAAS).getId();
//        aliasRequest = createPortion(CALVE_PINDAKAAS_BELEG_2, 20, "gram");
//        foodService.addPortion(CALVE_PINDAKAAS_FOOD_ID, aliasRequest);
//
//        String EI = "Ei hardgekookt";
//        String EI_STUK = "ei";
//        foodService.addFood(createAddFoodRequest(EI, MeasurementUnit.UNIT, EI_STUK, 58.0, 12.3, 9.1, 0.2, new ArrayList<>()));
//        aliasRequest = createPortion(EI_STUK, 58.0, "gram");
//        Long EI_ID = foodRepository.getFood(EI).getId();
//        foodService.addPortion(EI_ID, aliasRequest);
//
//        AddLogEntryRequest logEntry1 = new AddLogEntryRequest();
//        logEntry1.setDay(new DateTime(2018, 6, 21, 7, 0).toDate());
//        logEntry1.setFoodId(MUESLI_FOOD_ID);
//        logEntry1.setAliasIdUsed(portionRepository.getFoodAlias(MUESLI_FOOD_ID, MUESLI_SCHAAL).getAliasId());
//        logEntry1.setMultiplier(1.0);
//        logEntry1.setMeal("BREAKFAST");
//        logService.storeLogEntry(logEntry1);
//
//        AddLogEntryRequest logEntry2 = new AddLogEntryRequest();
//        logEntry2.setDay(new DateTime(2018, 6, 21, 7, 0).toDate());
//        logEntry2.setFoodId(YOGHURT_FOOD_ID);
//        logEntry2.setAliasIdUsed(portionRepository.getFoodAlias(YOGHURT_FOOD_ID, YOGHURT_VOL_SCHAAL).getAliasId());
//        logEntry2.setMultiplier(1.0);
//        logEntry2.setMeal("BREAKFAST");
//        logService.storeLogEntry(logEntry2);
//
//        AddLogEntryRequest logEntry3 = new AddLogEntryRequest();
//        logEntry3.setDay(new DateTime(2018, 6, 21, 7, 0).toDate());
//        logEntry3.setFoodId(EI_ID);
//        logEntry3.setAliasIdUsed(portionRepository.getFoodAlias(EI_ID, EI_STUK).getAliasId());
//        logEntry3.setMultiplier(4.0);
//        logEntry3.setMeal("LUNCH");
//        logService.storeLogEntry(logEntry3);
//
//        AddLogEntryRequest logEntry4 = new AddLogEntryRequest();
//        logEntry4.setDay(new DateTime(2018, 6, 21, 7, 0).toDate());
//        logEntry4.setFoodId(BROOD_ID);
//        logEntry4.setAliasIdUsed(portionRepository.getFoodAlias(BROOD_ID, BROOD_SNEE).getAliasId());
//        logEntry4.setMultiplier(4.0);
//        logEntry4.setMeal("LUNCH");
//        logService.storeLogEntry(logEntry4);
//
//        AddLogEntryRequest logEntry5 = new AddLogEntryRequest();
//        logEntry5.setDay(new DateTime(2018, 6, 21, 7, 0).toDate());
//        logEntry5.setFoodId(CALVE_PINDAKAAS_FOOD_ID);
//        logEntry5.setAliasIdUsed(portionRepository.getFoodAlias(CALVE_PINDAKAAS_FOOD_ID, CALVE_PINDAKAAS_BELEG_2).getAliasId());
//        logEntry5.setMultiplier(2.0);
//        logEntry5.setMeal("LUNCH");
//        logService.storeLogEntry(logEntry5);
    }

//    private static csl.dto.Portion createPortion(String description, double unitMultiplier, double grams) {
//        csl.dto.Portion portion = new csl.dto.Portion();
//        portion.setDescription(description);
//        portion.setUnitMultiplier(unitMultiplier);
//        portion.setGrams(grams);
//        return portion;
//    }

//    private static AddFoodRequest createAddFoodRequest(String name, MeasurementUnit typeUnit, double protein, double fat, double carbs) {
//        return createAddFoodRequest(name, typeUnit, null, 0.0, protein, fat, carbs, null);
//    }
//
//    private static AddFoodRequest createAddFoodRequest(String name, MeasurementUnit typeUnit, String unitName, double unitGrams,
//                                                       double protein, double fat, double carbs, List<Portion> portions) {
//        AddFoodRequest foodRequest = new AddFoodRequest();
//        foodRequest.setName(name);
//        foodRequest.setMeasurementUnit(typeUnit);
//        foodRequest.setUnitName(unitName);
//        foodRequest.setUnitGrams(unitGrams);
//        foodRequest.setProtein(protein);
//        foodRequest.setFat(fat);
//        foodRequest.setCarbs(carbs);
//        foodRequest.setPortions(portions);
//        return foodRequest;
//    }

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