package csl;

import csl.database.*;
import csl.dto.AddFoodRequest;
import csl.dto.StoreLogEntryRequest;
import csl.dto.PortionDto;
import csl.enums.MeasurementUnit;
import csl.rest.FoodService;
import csl.rest.LogEntryService;
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
        boolean clearTablesOnStartup = false;

        if (clearTablesOnStartup) {
            deleteTables();
        }

        boolean tableExists = isDatabaseSetUp();
        if (!tableExists) {
            createTables();
        } else {
            LOGGER.info("Tables already set up");
        }


        //Application is now running

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

    private static void setUpDatabase() {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));
        String checkUrl = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES where" +
                " table_name = '" + SettingsRepository.TABLE_NAME + "' or " +
                " table_name = '" + FoodRepository.TABLE_NAME + "' or " +
                " table_name = '" + PortionRepository.TABLE_NAME + "' or " +
                " table_name = '" + LogEntryRepository.TABLE_NAME + "' or " +
                " table_name = '" + MealRepository.TABLE_NAME + "' or " +
                " table_name = '" + IngredientRepository.TABLE_NAME + "'";
        List<String> results = template.query(checkUrl, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("TABLE_NAME");
            }
        });
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
        return results.size() == 4;
    }
}