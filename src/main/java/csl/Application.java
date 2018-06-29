package csl;

import csl.database.DatabaseHelper;
import csl.database.FoodAliasRepository;
import csl.database.FoodRepository;
import csl.dto.AddFoodMacroRequest;
import csl.dto.AddUnitAliasRequest;
import csl.dto.FoodMacros;
import csl.dto.Macro;
import csl.rest.FoodService;
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


            String brood = "Brood: Goudeerlijk bus fijn volkoren Jumbo";
            foodService.storeFood(brood, createAddFoodMacroRequest(11.1, 2.0, 38.0));
            ResponseEntity foodInformation = foodService.getFoodInformation(brood);
            FoodMacros broodMacro = (FoodMacros) foodInformation.getBody();
            LOGGER.debug("Brood : " + broodMacro);
            AddUnitAliasRequest aliasRequest = new AddUnitAliasRequest();
            aliasRequest.setAliasName("snee");
            aliasRequest.setAliasAmount(30.0);
            aliasRequest.setAliasUnitName("gram");

            foodService.addAlias(broodMacro.getFoodId(), aliasRequest);

            foodService.storeFood("Jordans Muesli de luxe ", createAddFoodMacroRequest(9.4, 9.2, 59.3));
            foodService.storeFood("Yoghurt vol Jumbo", createAddFoodMacroRequest(4.1, 3.1, 4.2));
            foodService.storeFood("Calvé pindakaas", createAddFoodMacroRequest(20, 55, 15));
            foodService.storeFood("Ei hardgekookt", createAddFoodMacroRequest(12.3, 9.1, 0.2));
//

        }

    }

    private static AddFoodMacroRequest createAddFoodMacroRequest(double proteins, double fat, double carbs) {
        return createAddFoodMacroRequest(proteins, fat, carbs, 100.0, "gram");
    }

    private static AddFoodMacroRequest createAddFoodMacroRequest(double proteins, double fat, double carbs, double defaultAmount, String defaultUnitname) {
        AddFoodMacroRequest myFood = new AddFoodMacroRequest();
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
            callableStatement = connection.prepareCall(FoodAliasRepository.TABLE_DELETE);
            execute = execute || callableStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
       try{
            callableStatement = connection.prepareCall(FoodRepository.TABLE_DELETE);
            execute = execute || callableStatement.execute();

            LOGGER.debug("Succes REMOVE: " + execute);
        } catch (SQLException e) {
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

            LOGGER.debug("Succes INSTALL: " + execute);
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