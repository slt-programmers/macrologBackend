package csl;

import csl.database.DatabaseHelper;
import csl.database.FoodAliasRepository;
import csl.database.FoodRepository;
import csl.database.LogEntryRepository;
import csl.dto.*;
import csl.rest.FoodService;
import csl.rest.LogsService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

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
        boolean automaticResetDatabase = true;

        boolean tableExists = isDatabaseOpgezet();

        if (tableExists && automaticResetDatabase) {
            deleteTables();
        }
//        if (true){
//            return;
//        }
        if (!tableExists || automaticResetDatabase) {
            createTables();

            FoodService foodService = new FoodService();
            FoodRepository foodRepository = new FoodRepository();
            FoodAliasRepository foodAliasRepository = new FoodAliasRepository();
            LogsService logService = new LogsService();


            String BROOD = "Brood: Goudeerlijk bus fijn volkoren Jumbo";
            String BROOD_SNEE = "snee";

            foodService.storeFood(BROOD, createAddFoodMacroRequest(11.1, 2.0, 38.0));
            Long BROOD_ID = foodRepository.getFood(BROOD).getId();

            AddUnitAliasRequest aliasRequest = getAddUnitAliasRequest(BROOD_SNEE, 30.0, "gram");
            foodService.addAlias(BROOD_ID, aliasRequest);

            String MUESLI = "Jordans Muesli de luxe";
            String MUESLI_SCHAAL = "schaal Muesli";

            foodService.storeFood(MUESLI, createAddFoodMacroRequest(9.4, 9.2, 59.3));
            Long MUESLI_FOOD_ID = foodRepository.getFood(MUESLI).getId();
            aliasRequest = getAddUnitAliasRequest(MUESLI_SCHAAL, 80, "gram");
            foodService.addAlias(MUESLI_FOOD_ID, aliasRequest);

            String YOGHURT_VOL_JUMBO = "Yoghurt vol Jumbo";
            String YOGHURT_VOL_SCHAAL = "schaal Yoghurt Vol";

            foodService.storeFood(YOGHURT_VOL_JUMBO, createAddFoodMacroRequest(4.1, 3.1, 4.2));
            Long YOGHURT_FOOD_ID = foodRepository.getFood(YOGHURT_VOL_JUMBO).getId();
            aliasRequest = getAddUnitAliasRequest(YOGHURT_VOL_SCHAAL, 200, "gram");
            foodService.addAlias(YOGHURT_FOOD_ID, aliasRequest);

            String CALVE_PINDAKAAS = "Calvé pindakaas";
            String CALVE_PINDAKAAS_BELEG_2 = "2 boterhammen pindakaas beleg";

            foodService.storeFood(CALVE_PINDAKAAS, createAddFoodMacroRequest(20, 55, 15));
            Long CALVE_PINDAKAAS_FOOD_ID = foodRepository.getFood(CALVE_PINDAKAAS).getId();

            aliasRequest = getAddUnitAliasRequest(CALVE_PINDAKAAS_BELEG_2, 20, "gram");
            foodService.addAlias(CALVE_PINDAKAAS_FOOD_ID, aliasRequest);

            String EI = "Ei hardgekookt";
            String EI_STUK = "ei";
            foodService.storeFood(EI, createAddFoodMacroRequest(12.3, 9.1, 0.2));
            aliasRequest = getAddUnitAliasRequest(EI_STUK, 58.0, "gram");
            Long EI_ID = foodRepository.getFood(EI).getId();
            foodService.addAlias(EI_ID, aliasRequest);

            AddLogEntryRequest logEntry1 = new AddLogEntryRequest();
            logEntry1.setDay(new DateTime(2018,6,21,7,0).toDate());
            logEntry1.setFoodId(MUESLI_FOOD_ID);
            logEntry1.setAliasIdUsed(foodAliasRepository.getFoodAlias(MUESLI_FOOD_ID,MUESLI_SCHAAL).getAliasId());
            logEntry1.setMultiplier(1.0);
            logEntry1.setMeal("BREAKFAST");
            logService.storeLogEntry(logEntry1);

            AddLogEntryRequest logEntry2 = new AddLogEntryRequest();
            logEntry2.setDay(new DateTime(2018,6,21,7,0).toDate());
            logEntry2.setFoodId(YOGHURT_FOOD_ID);
            logEntry2.setAliasIdUsed(foodAliasRepository.getFoodAlias(YOGHURT_FOOD_ID,YOGHURT_VOL_SCHAAL).getAliasId());
            logEntry2.setMultiplier(1.0);
            logEntry2.setMeal("BREAKFAST");
            logService.storeLogEntry(logEntry2);

            AddLogEntryRequest logEntry3 = new AddLogEntryRequest();
            logEntry3.setDay(new DateTime(2018,6,21,7,0).toDate());
            logEntry3.setFoodId(EI_ID);
            logEntry3.setAliasIdUsed(foodAliasRepository.getFoodAlias(EI_ID,EI_STUK).getAliasId());
            logEntry3.setMultiplier(4.0);
            logEntry3.setMeal("LUNCH");
            logService.storeLogEntry(logEntry3);


            AddLogEntryRequest logEntry4 = new AddLogEntryRequest();
            logEntry4.setDay(new DateTime(2018,6,21,7,0).toDate());
            logEntry4.setFoodId(BROOD_ID);
            logEntry4.setAliasIdUsed(foodAliasRepository.getFoodAlias(BROOD_ID,BROOD_SNEE).getAliasId());
            logEntry4.setMultiplier(4.0);
            logEntry4.setMeal("LUNCH");
            logService.storeLogEntry(logEntry4);

            AddLogEntryRequest logEntry5 = new AddLogEntryRequest();
            logEntry5.setDay(new DateTime(2018,6,21,7,0).toDate());
            logEntry5.setFoodId(CALVE_PINDAKAAS_FOOD_ID);
            logEntry5.setAliasIdUsed(foodAliasRepository.getFoodAlias(CALVE_PINDAKAAS_FOOD_ID,CALVE_PINDAKAAS_BELEG_2).getAliasId());
            logEntry5.setMultiplier(2.0);
            logEntry5.setMeal("LUNCH");
            logService.storeLogEntry(logEntry5);

        }

    }

    private static FoodMacros getFoodMacro(FoodService foodService, Long  foodId) {
        ResponseEntity foodInformation = foodService.getFoodInformation(foodId);
        return (FoodMacros) foodInformation.getBody();
    }

    private static AddUnitAliasRequest getAddUnitAliasRequest(String aliasname, double aliasAmount, String aliasUnit) {
        AddUnitAliasRequest aliasRequest = new AddUnitAliasRequest();
        aliasRequest.setAliasName(aliasname);
        aliasRequest.setAliasAmount(aliasAmount);
        aliasRequest.setAliasUnitName(aliasUnit);
        return aliasRequest;
    }

    private static AddFoodRequest createAddFoodMacroRequest(double proteins, double fat, double carbs) {
        return createAddFoodMacroRequest(proteins, fat, carbs, 100.0, "gram");
    }

    private static AddFoodRequest createAddFoodMacroRequest(double proteins, double fat, double carbs, double defaultAmount, String defaultUnitname) {
        AddFoodRequest myFood = new AddFoodRequest();
        myFood.setDefaultAmount(defaultAmount);
        myFood.setDefaultUnitname(defaultUnitname);
        Macro macros = new Macro(proteins, fat, carbs);
        myFood.setMacroPerUnit(macros);
        return myFood;
    }

    private static void deleteTables() {
        boolean execute = false;
        CallableStatement callableStatement = null;
        Connection connection = null;
        try {
            connection = DatabaseHelper.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            callableStatement = connection.prepareCall(LogEntryRepository.TABLE_DELETE);
            execute = execute || callableStatement.execute();
        } catch (SQLException e) {
            LOGGER.debug("Failed to delete table " + LogEntryRepository.TABLE_NAME);
            e.printStackTrace();
        }
        try {
            callableStatement = connection.prepareCall(FoodAliasRepository.TABLE_DELETE);
            execute = execute || callableStatement.execute();
        } catch (SQLException e) {
            LOGGER.debug("Failed to delete table " + FoodAliasRepository.TABLE_NAME);
            e.printStackTrace();
        }
       try{
            callableStatement = connection.prepareCall(FoodRepository.TABLE_DELETE);
            execute = execute || callableStatement.execute();

            LOGGER.debug("Succes REMOVE");
        } catch (SQLException e) {
           LOGGER.debug("Failed to delete table " + FoodRepository.TABLE_NAME);
            e.printStackTrace();
        }
    }

    private static void createTables() {
        try {
            Connection connection = DatabaseHelper.getInstance().getConnection();

            CallableStatement callableStatement = connection.prepareCall(FoodRepository.TABLE_CREATE);
            boolean execute = callableStatement.execute();

            callableStatement = connection.prepareCall(FoodAliasRepository.TABLE_CREATE);
            execute = execute || callableStatement.execute();

            callableStatement = connection.prepareCall(LogEntryRepository.TABLE_CREATE);
            execute = execute || callableStatement.execute();

            LOGGER.debug("Succes INSTALL");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Controle dmv kijken of tabellen bestaan
     *
     * @return
     * @throws SQLException
     */
    private static boolean isDatabaseOpgezet() throws SQLException {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

        String checkUrl = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES where table_name = '" + FoodRepository.TABLE_NAME + "'";
        List<String> results = template.query(checkUrl, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("TABLE_NAME");
            }
        });
        return results.size() == 1;
    }
}