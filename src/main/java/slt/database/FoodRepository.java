package slt.database;

import slt.database.model.Food;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class FoodRepository {
    public static final String TABLE_NAME = "food";

    public static final String COL_ID = "id";
    private final String COL_USER_ID = "user_id";
    private final String COL_NAME = "name";
    private final String COL_PROTEIN = "protein";
    private final String COL_FAT = "fat";
    private final String COL_CARBS = "carbs";

    private static final String SELECT_SQL = "SELECT * FROM " + TABLE_NAME;
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + "(user_id, name, protein, fat, carbs) VALUES(:userId, :name, :protein, :fat, :carbs)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + " SET name = :name, protein = :protein, fat = :fat, carbs = :carbs WHERE id = :id AND user_id = :userId";
    private static final String DELETE_ALL_SQL = "DELETE FROM " + TABLE_NAME + " WHERE user_id = :userId";

    @Autowired
    DatabaseHelper databaseHelper;

    @PostConstruct
    private void initTemplate() {
        template = new NamedParameterJdbcTemplate(new JdbcTemplate(databaseHelper));
    }

    private NamedParameterJdbcTemplate template;

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
        return template.query(SELECT_SQL + " WHERE " + COL_USER_ID + " = :userId", params, new FoodWrapper<Food>());
    }

    public int insertFood(Integer userId, Food food) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("userId", userId)
                .addValue("name", food.getName())
                .addValue("protein", food.getProtein())
                .addValue("fat", food.getFat())
                .addValue("carbs", food.getCarbs());
        return template.update(INSERT_SQL, params);
    }

    public int updateFood(Integer userId, Food food) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", food.getId())
                .addValue("userId", userId)
                .addValue("name", food.getName())
                .addValue("protein", food.getProtein())
                .addValue("fat", food.getFat())
                .addValue("carbs", food.getCarbs());
        return template.update(UPDATE_SQL, params);
    }

    public Food getFood(Integer userId, String name) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("name", name);
        String myFood = SELECT_SQL + " WHERE  " + COL_NAME + " = :name AND " + COL_USER_ID + " = :userId";
        List<Food> queryResults = template.query(myFood, params, new FoodWrapper<Food>());
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public Food getFoodById(Integer userId, Long id) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", id);
        String myFood = SELECT_SQL + " WHERE  " + COL_ID + " = :id AND " + COL_USER_ID + " = :userId";
        List<Food> queryResults = template.query(myFood, params, new FoodWrapper<Food>());
        Assert.isTrue(queryResults.size() <= 1, "More than one food was found");
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public int deleteAllForUser(Integer userId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        return template.update(DELETE_ALL_SQL, params);
    }

    class FoodWrapper<T> implements RowMapper<Food> {

        @Override
        public Food mapRow(ResultSet rs, int i) throws SQLException {
            return new Food(rs.getLong(COL_ID),
                    rs.getString(COL_NAME),
                    rs.getDouble(COL_PROTEIN),
                    rs.getDouble(COL_FAT),
                    rs.getDouble(COL_CARBS)
            );
        }
    }
}

