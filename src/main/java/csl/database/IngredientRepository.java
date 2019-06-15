package csl.database;

import csl.database.model.Ingredient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class IngredientRepository {

    public static final String TABLE_NAME = "ingredient";

    private static final String COL_ID = "id";
    private static final String COL_MEAL_ID = "meal_id";
    private static final String COL_FOOD_ID = "food_id";
    private static final String COL_PORTION_ID = "portion_id";
    private static final String COL_MULTIPLIER = "multiplier";

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_MEAL_ID + " INT(6) NOT NULL, " +
                    COL_FOOD_ID + " INT(6) NOT NULL, " +
                    COL_PORTION_ID + " INT(6), " +
                    "FOREIGN KEY (" + COL_MEAL_ID + ") REFERENCES " + MealRepository.TABLE_NAME + "(" + MealRepository.COL_ID + "), " +
                    "FOREIGN KEY (" + COL_FOOD_ID + ") REFERENCES " + FoodRepository.TABLE_NAME + "(" + FoodRepository.COL_ID + "), " +
                    "FOREIGN KEY (" + COL_PORTION_ID + ") REFERENCES " + PortionRepository.TABLE_NAME + "(" + PortionRepository.COL_ID + "), " +
                    COL_MULTIPLIER + " DEC(5,2) NOT NULL" +
                    ")";

    private static final String SELECT_SQL = "SELECT * FROM " + TABLE_NAME;
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + "(meal_id, food_Id, portion_Id, multiplier) VALUES(:mealId, :foodId, :portionId, :multiplier)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + " SET meal_id = :mealId, food_id = :foodId, portion_id = :portionId, multiplier = :multiplier WHERE id = :id";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME;

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

    int insertIngredient(Ingredient ingredient) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("mealId", ingredient.getMealId())
                .addValue("foodId", ingredient.getFoodId())
                .addValue("portionId", ingredient.getPortionId())
                .addValue("multiplier", ingredient.getMultiplier());
        return template.update(INSERT_SQL, params);
    }

    void updateIngredientsForMeal(Long mealId, List<Ingredient> newIngredients) {
        List<Ingredient> currentList = getAllIngredientsForMeal(mealId);

        for (Ingredient ingredient : newIngredients) {
            if (ingredient.getId() == null) {
                insertIngredient(ingredient);
            } else {
                updateIngredient(ingredient);
            }
        }

        for (Ingredient ingredient : currentList) {
            Long id = ingredient.getId();
            boolean found = false;
            for (Ingredient newIngredient : newIngredients) {
                if (newIngredient.getId().equals(id)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                deleteIngredient(ingredient.getId());
            }
        }
    }

    int updateIngredient(Ingredient ingredient) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", ingredient.getId())
                .addValue("mealId", ingredient.getMealId())
                .addValue("foodId", ingredient.getFoodId())
                .addValue("portionId", ingredient.getPortionId())
                .addValue("multiplier", ingredient.getMultiplier());
        return template.update(UPDATE_SQL, params);
    }

    int deleteIngredientsForMeal(Long mealId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("mealId", mealId);
        return template.update(DELETE_SQL + " WHERE " + COL_MEAL_ID + " = :mealId", params);
    }

    int deleteIngredient(Long ingredientId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", ingredientId);
        return template.update(DELETE_SQL + " WHERE " + COL_ID + " = :id", params);
    }

    List<Ingredient> getAllIngredientsForMeal(Long mealId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("mealId", mealId);
        return template.query(SELECT_SQL + " WHERE " + COL_MEAL_ID + " = :mealId", params, new IngredientWrapper());
    }

    class IngredientWrapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            return new Ingredient(rs.getLong(COL_ID),
                    rs.getLong(COL_MEAL_ID),
                    rs.getLong(COL_FOOD_ID),
                    rs.getLong(COL_PORTION_ID),
                    rs.getDouble(COL_MULTIPLIER));

        }
    }
}