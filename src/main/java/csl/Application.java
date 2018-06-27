package csl;

import csl.database.DatabaseHelper;
import csl.database.FoodRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        if (tableExists && automaticResetDatabase){
            deleteTables();
        }
        if (!tableExists || automaticResetDatabase) {
            createTables();
        }

    }

    private static void deleteTables() {
        try {
            Connection connection = DatabaseHelper.getInstance().getConnection();
            CallableStatement callableStatement = connection.prepareCall(FoodRepository.TABLE_DELETE);
            boolean execute = callableStatement.execute();
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
            LOGGER.debug("Succes INSTALL: " + execute);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


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