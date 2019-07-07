package csl;

import csl.database.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
@AutoConfigureBefore(DatabaseConfig.class)
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
//        setUpDatabase();
        LOGGER.debug("Application is now running.");
    }
//
//    private static void setUpDatabase() {
//        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));
//        String checkUrl = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES where" +
//                " table_name = '" + UserAcccountRepository.TABLE_NAME + "' or " +
//                " table_name = '" + SettingsRepository.TABLE_NAME + "' or " +
//                " table_name = '" + FoodRepository.TABLE_NAME + "' or " +
//                " table_name = '" + PortionRepository.TABLE_NAME + "' or " +
//                " table_name = '" + LogEntryRepository.TABLE_NAME + "' or " +
//                " table_name = '" + MealRepository.TABLE_NAME + "' or " +
//                " table_name = '" + ActivityRepository.TABLE_NAME + "' or " +
//                " table_name = '" + WeightRepository.TABLE_NAME + "' or " +
//                " table_name = '" + IngredientRepository.TABLE_NAME + "'";
//        List<String> existingTables = template.query(checkUrl, (resultSet, i) -> resultSet.getString("TABLE_NAME"));
//
//        if (!existingTables.contains(UserAcccountRepository.TABLE_NAME)) {
//            LOGGER.info("Create useraccounts table");
//            createTable(UserAcccountRepository.TABLE_CREATE);
//        }
//        if (!existingTables.contains(SettingsRepository.TABLE_NAME)) {
//            LOGGER.info("Create settings table");
//            createTable(SettingsRepository.TABLE_CREATE);
//        }
//        if (!existingTables.contains(FoodRepository.TABLE_NAME)) {
//            LOGGER.info("Create food table");
//            createTable(FoodRepository.TABLE_CREATE);
//        }
//        if (!existingTables.contains(PortionRepository.TABLE_NAME)) {
//            LOGGER.info("Create portion table");
//            createTable(PortionRepository.TABLE_CREATE);
//        }
//        if (!existingTables.contains(LogEntryRepository.TABLE_NAME)) {
//            LOGGER.info("Create logentry table");
//            createTable(LogEntryRepository.TABLE_CREATE);
//        }
//        if (!existingTables.contains(MealRepository.TABLE_NAME)) {
//            LOGGER.info("Create meal table");
//            createTable(MealRepository.TABLE_CREATE);
//        }
//        if (!existingTables.contains(IngredientRepository.TABLE_NAME)) {
//            LOGGER.info("Create ingredient table");
//            createTable(IngredientRepository.TABLE_CREATE);
//        }
//        if (!existingTables.contains(ActivityRepository.TABLE_NAME)) {
//            LOGGER.info("Create activity table");
//            createTable(ActivityRepository.TABLE_CREATE);
//        }
//
//        if (!existingTables.contains(WeightRepository.TABLE_NAME)) {
//            LOGGER.info("Create weight table");
//            createTable(WeightRepository.TABLE_CREATE);
//        }
//
//    }

//    private static void createTable(String sql) {
//        System.out.println(sql);
//        try (Connection connection = DatabaseHelper.getInstance().getConnection();
//             CallableStatement tableCreate = connection.prepareCall(sql)) {
//            tableCreate.execute();
//            LOGGER.info("Table created succesfully");
//        } catch (SQLException e) {
//            LOGGER.error("Failed to create table");
//            LOGGER.error(e.getMessage());
//        }
//    }
}
