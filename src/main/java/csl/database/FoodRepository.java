package csl.database;

import csl.database.model.Food;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Carmen on 18-3-2018.
 */
@Repository
public class FoodRepository {
    public static final String TABLE_NAME = "food";

    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_PROTEINS = "proteins";
    public static final String COL_FATS = "fats";
    public static final String COL_CARBS = "carbs";
    public static final String COL_PER = "per";

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_NAME + " TEXT NOT NULL, " +
                    COL_PROTEINS + " DEC(5,2)  NOT NULL, " +
                    COL_FATS + " DEC(5,2) NOT NULL, " +
                    COL_CARBS + " DEC(5,2) NOT NULL," +
                    COL_PER + " TEXT NOT NULL)" ;

    public static final String TABLE_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SELECT_SQL = "select * from food";
    private static final String INSERT_SQL = "insert into food( name,proteins,fats,carbs,per) values(:name,:protein,:fats,:carbs,:per)";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

    public FoodRepository() {


        String checkUrl = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES where table_name = '"+TABLE_NAME+"'";

        List<String> results = template.query(checkUrl, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("TABLE_NAME");
            }
        });
        if (results.size() == 0){
            System.out.println("Create table");
            try {
                Connection connection = DatabaseHelper.getInstance().getConnection();
                CallableStatement callableStatement = connection.prepareCall(TABLE_CREATE);
                boolean execute = callableStatement.execute();
                System.out.println("Succes installatie: " + execute);
           } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    public List<Food> getAllFood() {
        return template.query(SELECT_SQL, new FoodWrapper());
    }

    public int insertFood(Food food) {
        SqlParameterSource params = new MapSqlParameterSource()
               .addValue("id", null)
                .addValue("name", food.getName())
                .addValue("protein", food.getProtein())
                .addValue("fats", food.getFat())
                .addValue("per", "100g")
                .addValue("carbs", food.getCarbs());
        return template.update(INSERT_SQL, params);
    }

    public List<Food> getFood(String name) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name);
        String myFood = SELECT_SQL + " WHERE  "+ COL_NAME + "= :name";
        List<Food> queryResults = template.query(myFood, params, new FoodWrapper());
        return queryResults;
    }
}

class FoodWrapper implements RowMapper {

    @Override
    public Object mapRow(ResultSet rs, int i) throws SQLException {
        return new Food(rs.getString("name"),
                rs.getString("per"),
                rs.getDouble("proteins"),
                rs.getDouble("fats"),
                rs.getDouble("carbs")
                );
    }
}