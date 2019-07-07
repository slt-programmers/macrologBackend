package csl.database;

import csl.database.model.Ingredient;
import csl.database.model.Meal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MealRepository {
    private static final IngredientRepository ingredientRepository = new IngredientRepository();

    public static final String TABLE_NAME = "meal";

    static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_NAME = "name";

    private static final String SELECT_SQL = "SELECT * FROM " + TABLE_NAME;
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + "(name, user_id) VALUES(:name, :userId)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + " SET name = :name WHERE Id = :id AND user_id = :userId";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE id = :id AND user_id = :userId";
    private static final String DELETE_ALL_SQL = "DELETE FROM " + TABLE_NAME + " WHERE user_id = :userId";

    @Autowired
    DatabaseHelper databaseHelper;

    @PostConstruct
    private void initTemplate() {
        template = new NamedParameterJdbcTemplate(new JdbcTemplate(databaseHelper));
    }

    private NamedParameterJdbcTemplate template;

    public int insertMeal(Integer userId, Meal meal) {

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("userId", userId)
                .addValue("name", meal.getName());
        int result = template.update(INSERT_SQL, params);

        Long mealId = getMealByName(userId, meal.getName()).getId();
        System.out.println(mealId);
        for (Ingredient ingredient : meal.getIngredients()) {
            ingredient.setMealId(mealId);
            System.out.println(ingredient.getMealId());
            ingredientRepository.insertIngredient(ingredient);
        }
        return result;
    }

    public int updateMeal(Integer userId, Meal meal) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", meal.getId())
                .addValue("userId", userId)
                .addValue("name", meal.getName());
        ingredientRepository.updateIngredientsForMeal(meal.getId(), meal.getIngredients());
        return template.update(UPDATE_SQL, params);
    }

    public int deleteMeal(Integer userId, Long mealId) {
        ingredientRepository.deleteIngredientsForMeal(mealId);
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", mealId);
        return template.update(DELETE_SQL, params);
    }

    private Meal getMealByName(Integer userId, String name) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("name", name);
        List<Meal> result = template.query(SELECT_SQL + " WHERE " + COL_NAME + " = :name AND " + COL_USER_ID + " = :userId", params, new MealWrapper());
        return result.get(0);
    }

    public Meal getMeal(Integer userId, Long mealId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", mealId);
        List<Meal> result = template.query(SELECT_SQL + " WHERE " + COL_ID + " = :id AND " + COL_USER_ID + " = :userId", params, new MealWrapper());
        return result.get(0);
    }

    public List<Meal> getAllMeals(Integer userId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        return template.query(SELECT_SQL + " WHERE " + COL_USER_ID + " = :userId", params, new MealWrapper());
    }

    public int deleteAllForUser(Integer userId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        return template.update(DELETE_ALL_SQL, params);
    }

    class MealWrapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            Long mealId = rs.getLong(COL_ID);
            List<Ingredient> ingredients = ingredientRepository.getAllIngredientsForMeal(mealId);
            return new Meal(
                    mealId,
                    rs.getString(COL_NAME),
                    ingredients);

        }
    }
}