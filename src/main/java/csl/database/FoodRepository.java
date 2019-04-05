package csl.database;

import csl.database.model.Food;
import csl.enums.MeasurementUnit;
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

/**
 * Created by Carmen on 18-3-2018.
 */
@Repository
public class FoodRepository {
    public static final String TABLE_NAME = "food";

    public static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_NAME = "name";
    private static final String COL_MEASUREMENT = "measurement";
    private static final String COL_PROTEIN = "protein";
    private static final String COL_FAT = "fat";
    private static final String COL_CARBS = "carbs";
    private static final String COL_UNIT_NAME = "unit_name";
    private static final String COL_UNIT_GRAMS = "unit_grams";

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_USER_ID + " INT(6) NOT NULL, " +
                    COL_NAME + " TEXT NOT NULL, " +
                    COL_MEASUREMENT + " TEXT NOT NULL, " +
                    COL_PROTEIN + " DEC(5,2)  NOT NULL, " +
                    COL_FAT + " DEC(5,2) NOT NULL, " +
                    COL_CARBS + " DEC(5,2) NOT NULL," +
                    COL_UNIT_NAME + " TEXT," +
                    COL_UNIT_GRAMS + " DEC(5,2)," +
                    "FOREIGN KEY (" + COL_USER_ID + ") REFERENCES " + UserAcccountRepository.TABLE_NAME + "(" + UserAcccountRepository.COL_ID + ")" +
                    ")";

    public static final String TABLE_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SELECT_SQL = "select * from food";
    private static final String INSERT_SQL = "insert into food(" +
            "user_id,name, measurement, protein, fat, carbs, unit_name,unit_grams) " +
            "values(:userId, :name, :measurement, :protein, :fat, :carbs, :unit_name,:unit_grams)";
    private static final String UPDATE_SQL = "update food set " +
            "name = :name, measurement = :measurement, protein = :protein, fat = :fat, carbs = :carbs, unit_name = :unit_name ,unit_grams = :unit_grams " +
            "where id = :id AND user_id = :userId";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

    public FoodRepository() {
    }

    private List<Food> getAllFood() {
        return template.query(SELECT_SQL, new FoodWrapper<Food>());
    }

    public List<Food> getSomeFood(String selectStatement) {
        return template.query(selectStatement, new FoodWrapper<Food>());
    }

    public List<Food> getAllFood(Integer userId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        return template.query(SELECT_SQL + " WHERE user_id=:userId",params, new FoodWrapper<Food>());
    }

    public int insertFood(Integer userId, Food food) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("userId", userId)
                .addValue("name", food.getName())
                .addValue("measurement", food.getMeasurementUnit().toString())
                .addValue("protein", food.getProtein())
                .addValue("fat", food.getFat())
                .addValue("carbs", food.getCarbs())
                .addValue("unit_name", food.getUnitName())
                .addValue("unit_grams", food.getUnitGrams());
        return template.update(INSERT_SQL, params);
    }

    public int updateFood(Integer userId, Food food) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", food.getId())
                .addValue("userId", userId)
                .addValue("name", food.getName())
                .addValue("measurement", food.getMeasurementUnit().toString())
                .addValue("protein", food.getProtein())
                .addValue("fat", food.getFat())
                .addValue("carbs", food.getCarbs())
                .addValue("unit_name", food.getUnitName())
                .addValue("unit_grams", food.getUnitGrams());
        return template.update(UPDATE_SQL, params);
    }

    public Food getFood(Integer userId, String name) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("name", name);
        String myFood = SELECT_SQL + " WHERE  " + COL_NAME + "= :name AND user_id=:userId";
        List<Food> queryResults = template.query(myFood, params, new FoodWrapper<Food>());
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public Food getFoodById(Integer userId, Long id) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", id);
        String myFood = SELECT_SQL + " WHERE  " + COL_ID + "= :id AND user_id=:userId";
        List<Food> queryResults = template.query(myFood, params, new FoodWrapper<Food>());
        Assert.isTrue(queryResults.size() <= 1, "More than one food was found");
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    class FoodWrapper<T> implements RowMapper<Food> {

        @Override
        public Food mapRow(ResultSet rs, int i) throws SQLException {
            return new Food(rs.getLong(COL_ID),
                    rs.getString(COL_NAME),
                    MeasurementUnit.valueOf(rs.getString(COL_MEASUREMENT)),
                    rs.getDouble(COL_PROTEIN),
                    rs.getDouble(COL_FAT),
                    rs.getDouble(COL_CARBS),
                    rs.getString(COL_UNIT_NAME),
                    rs.getDouble(COL_UNIT_GRAMS)
            );
        }
    }
}

