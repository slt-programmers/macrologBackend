package csl;

import csl.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

        setUpDatabase();
        LOGGER.debug("Application is now running.");
    }


    private static void setUpDatabase() {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));
        String checkUrl = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES where" +
                " table_name = '" + UserAcccountRepository.TABLE_NAME + "' or " +
                " table_name = '" + SettingsRepository.TABLE_NAME + "' or " +
                " table_name = '" + FoodRepository.TABLE_NAME + "' or " +
                " table_name = '" + PortionRepository.TABLE_NAME + "' or " +
                " table_name = '" + LogEntryRepository.TABLE_NAME + "' or " +
                " table_name = '" + MealRepository.TABLE_NAME + "' or " +
                " table_name = '" + IngredientRepository.TABLE_NAME + "'";
        List<String> existingTables = template.query(checkUrl, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("TABLE_NAME");
            }
        });

        if (!existingTables.contains(UserAcccountRepository.TABLE_NAME)) {
            LOGGER.info("Create useraccounts table");
            createTable(UserAcccountRepository.TABLE_CREATE);
        }
        if (!existingTables.contains(SettingsRepository.TABLE_NAME)) {
            LOGGER.info("Create settings table");
            createTable(SettingsRepository.TABLE_CREATE);
        }
        if (!existingTables.contains(FoodRepository.TABLE_NAME)) {
            LOGGER.info("Create food table");
            createTable(FoodRepository.TABLE_CREATE);
        }
        if (!existingTables.contains(PortionRepository.TABLE_NAME)) {
            LOGGER.info("Create portion table");
            createTable(PortionRepository.TABLE_CREATE);
        }
        if (!existingTables.contains(LogEntryRepository.TABLE_NAME)) {
            LOGGER.info("Create logentry table");
            createTable(LogEntryRepository.TABLE_CREATE);
        }
        if (!existingTables.contains(MealRepository.TABLE_NAME)) {
            LOGGER.info("Create meal table");
            createTable(MealRepository.TABLE_CREATE);
        }
        if (!existingTables.contains(IngredientRepository.TABLE_NAME)) {
            LOGGER.info("Create ingredient table");
            createTable(IngredientRepository.TABLE_CREATE);
        }

//        updateAT();
//        updateWeightSettings();
//        updateUserAccountsWithEmail();
//          updateSettings();
    }

    private static void createTable(String sql) {
        System.out.println(sql);
        try (Connection connection = DatabaseHelper.getInstance().getConnection();
             CallableStatement tableCreate = connection.prepareCall(sql)) {
            tableCreate.execute();
            LOGGER.info("Table created succesfully");
        } catch (SQLException e) {
            LOGGER.error("Failed to create table");
            LOGGER.error(e.getMessage());
        }
    }

    private static void updateWeightSettings() {
        String sql = "ALTER TABLE " + SettingsRepository.TABLE_NAME +
                " ADD COLUMN date DATE";
        try (Connection connection = DatabaseHelper.getInstance().getConnection();
             CallableStatement currStatement = connection.prepareCall(sql)) {
            LOGGER.info(sql);
            currStatement.execute();

        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private static void updateAT() {
        String[] statements = new String[]{"insert into useraccounts(id,username,password) values (null,'test','test')",
                // settings tabel:
                "ALTER TABLE " + SettingsRepository.TABLE_NAME + " ADD COLUMN USER_ID INT (6)",
                "UPDATE " + SettingsRepository.TABLE_NAME + " SET USER_ID = (select id from useraccounts where username='test')",
                "ALTER TABLE " + SettingsRepository.TABLE_NAME + " MODIFY COLUMN USER_ID INT (6) NOT NULL",
                "ALTER TABLE " + SettingsRepository.TABLE_NAME + " ADD FOREIGN KEY (USER_ID) REFERENCES " + UserAcccountRepository.TABLE_NAME + "(id)",
                // Food tabel:
                "ALTER TABLE " + FoodRepository.TABLE_NAME + " ADD COLUMN USER_ID INT (6)",
                "UPDATE " + FoodRepository.TABLE_NAME + " SET USER_ID = (select id from useraccounts where username='test')",
                "ALTER TABLE " + FoodRepository.TABLE_NAME + " MODIFY COLUMN USER_ID INT (6) NOT NULL",
                "ALTER TABLE " + FoodRepository.TABLE_NAME + " ADD FOREIGN KEY (USER_ID) REFERENCES " + UserAcccountRepository.TABLE_NAME + "(id)",
                // logentry tabel:
                "ALTER TABLE " + LogEntryRepository.TABLE_NAME + " ADD COLUMN USER_ID INT (6)",
                "UPDATE " + LogEntryRepository.TABLE_NAME + " SET USER_ID = (select id from useraccounts where username='test')",
                "ALTER TABLE " + LogEntryRepository.TABLE_NAME + " MODIFY COLUMN USER_ID INT (6) NOT NULL",
                "ALTER TABLE " + LogEntryRepository.TABLE_NAME + " ADD FOREIGN KEY (USER_ID) REFERENCES " + UserAcccountRepository.TABLE_NAME + "(id)",
                // meal tabel:
                "ALTER TABLE " + MealRepository.TABLE_NAME + " ADD COLUMN USER_ID INT (6)",
                "UPDATE " + MealRepository.TABLE_NAME + " SET USER_ID = (select id from useraccounts where username='test')",
                "ALTER TABLE " + MealRepository.TABLE_NAME + " MODIFY COLUMN USER_ID INT (6) NOT NULL",
                "ALTER TABLE " + MealRepository.TABLE_NAME + " ADD FOREIGN KEY (USER_ID) REFERENCES " + UserAcccountRepository.TABLE_NAME + "(id)"
        };

        runStatements(statements);
    }

    private static void updateUserAccountsWithEmail() {
        String sql = "ALTER TABLE " + UserAcccountRepository.TABLE_NAME +
                " ADD COLUMN email TEXT NOT NULL";
        try (Connection connection = DatabaseHelper.getInstance().getConnection();
             CallableStatement currStatement = connection.prepareCall(sql)) {
            LOGGER.info(sql);
            currStatement.execute();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private static void updateSettings(){
        String[] statements = new String[]{"alter table settings modify column setting text(50)",
        "ALTER TABLE settings ADD UNIQUE user_set(user_id, setting(50))"};
        runStatements(statements);
    }

    private static void runStatements(String[] statements) {
        LOGGER.debug("Running update scripts:");
        for (String statement : statements) {
            try (Connection connection = DatabaseHelper.getInstance().getConnection();
                 CallableStatement currStatement = connection.prepareCall(statement)) {
                LOGGER.info(statement);
                currStatement.execute();

            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
            }
        }
        LOGGER.debug("Finished running update scripts.");
    }
}