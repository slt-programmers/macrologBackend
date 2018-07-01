package csl.database;

import csl.database.model.Food;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static csl.database.FoodRepository.*;

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
    public static final String COL_DEFAULT_AMOUNT = "amount";
    public static final String COL_DEFAULT_AMOUNT_UNIT = "unitname";

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_NAME + " TEXT NOT NULL, " +
                    COL_PROTEINS + " DEC(5,2)  NOT NULL, " +
                    COL_FATS + " DEC(5,2) NOT NULL, " +
                    COL_CARBS + " DEC(5,2) NOT NULL," +
                    COL_DEFAULT_AMOUNT + " DEC(5,2) NOT NULL," +
                    COL_DEFAULT_AMOUNT_UNIT + " TEXT NOT NULL)";

    public static final String TABLE_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SELECT_SQL = "select * from food";
    private static final String INSERT_SQL = "insert into food( name,proteins,fats,carbs,amount,unitname) values(:name,:protein,:fats,:carbs,:amount,:unitname)";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

    public FoodRepository() {


    }

    public List<Food> getAllFood() {
        return template.query(SELECT_SQL, new FoodWrapper());
    }

    public int insertFood(Food food) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("name", food.getName())
                .addValue("amount", food.getAmountNumber())
                .addValue("unitname", food.getAmountUnit())
                .addValue("protein", food.getProtein())
                .addValue("fats", food.getFat())
                .addValue("carbs", food.getCarbs());
        return template.update(INSERT_SQL, params);
    }

    public Food getFood(String name) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name);
        String myFood = SELECT_SQL + " WHERE  " + COL_NAME + "= :name";
        List<Food> queryResults = template.query(myFood, params, new FoodWrapper());
        return queryResults.isEmpty()?null:queryResults.get(0);
    }
    public Food getFoodById(Long id) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);
        String myFood = SELECT_SQL + " WHERE  " + COL_ID+ "= :id";
        List<Food> queryResults = template.query(myFood, params, new FoodWrapper());
        Assert.isTrue(queryResults.size() <=1);
        return queryResults.isEmpty()?null:queryResults.get(0);
    }
}

class FoodWrapper implements RowMapper {

    @Override
    public Object mapRow(ResultSet rs, int i) throws SQLException {
        return new Food(rs.getLong(COL_ID),
                rs.getString(COL_NAME),
                rs.getDouble(COL_DEFAULT_AMOUNT),
                rs.getString(COL_DEFAULT_AMOUNT_UNIT),
                rs.getDouble(COL_PROTEINS),
                rs.getDouble(COL_FATS),
                rs.getDouble(COL_CARBS)
        );
    }
}