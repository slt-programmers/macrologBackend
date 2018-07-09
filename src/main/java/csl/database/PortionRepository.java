package csl.database;

import csl.database.model.Food;
import csl.database.model.Portion;
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
public class PortionRepository {
    public static final String TABLE_NAME = "portion";

    public static final String COL_ID = "id";
    public static final String COL_FOOD_ID = "food_id";
    public static final String COL_DESCRIPTION = "description";
    /* Times the unit. */
    public static final String COL_UNIT_MULTIPLIER = "unitmultiplier";
    /* If food has been defined by unit this may be empty */
    public static final String COL_GRAMS = "grams";

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_FOOD_ID + " INT(6) NOT NULL, " +
                    COL_DESCRIPTION + " TEXT NOT NULL, " +
                    COL_UNIT_MULTIPLIER + " DEC(5,2)," +
                    COL_GRAMS + " DEC(5,2)," +
                    "FOREIGN KEY (" + COL_FOOD_ID + ") REFERENCES " + FoodRepository.TABLE_NAME + "(" + FoodRepository.COL_ID + ")" +
                    ")";

    public static final String TABLE_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SELECT_SQL = "select * from " + TABLE_NAME;
    private static final String INSERT_SQL = "insert into " + TABLE_NAME + "( food_Id,description,unitmultiplier,grams) values(:foodId,:description,:unitmultiplier,:grams)";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

    public PortionRepository() {


    }

//    public List<Portion> getAllPortions() {
//        return template.query(SELECT_SQL, new PortionWrapper());
//    }

    public int addPortion(Food food, Portion portion) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("foodId", food.getId())
                .addValue("description", portion.getDescription())
                .addValue("unitmultiplier", portion.getUnitMultiplier())
                .addValue("grams", portion.getGrams());
        return template.update(INSERT_SQL, params);
    }

//    public List<Portion> getPortion(String name) {
//        SqlParameterSource params = new MapSqlParameterSource()
//                .addValue("aliasname", name);
//        String myFoodAlias = SELECT_SQL + " WHERE  " + COL_DESCRIPTION + "= :aliasname";
//        List<Portion> queryResults = template.query(myFoodAlias, params, new PortionWrapper());
//        return queryResults;
//    }

    public Portion getPortion(Long portionId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("portionId", portionId);
        String myFoodAlias = SELECT_SQL + " WHERE  " + COL_ID + "= :portionId";
        List<Portion> queryResults = template.query(myFoodAlias, params, new PortionWrapper());
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public Portion getPortion(Long foodId, String description) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("description", description)
                .addValue("foodId", foodId);
        String myFoodAlias = SELECT_SQL + " WHERE  " + COL_DESCRIPTION + "= :description AND " + COL_FOOD_ID + "=:foodId";
        List<Portion> queryResults = template.query(myFoodAlias, params, new PortionWrapper());
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public List<Portion> getPortions(Long foodId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("foodId", foodId);
        String myPortions = SELECT_SQL + " WHERE  " + COL_FOOD_ID + "= :foodId";
        List<Portion> queryResults = template.query(myPortions, params, new PortionWrapper());
        return queryResults;
    }

    class PortionWrapper implements RowMapper {

        @Override
        public Portion mapRow(ResultSet rs, int i) throws SQLException {
            return new Portion(rs.getLong(COL_ID),
                    rs.getString(COL_DESCRIPTION),
                    rs.getDouble(COL_GRAMS),
                    rs.getDouble(COL_UNIT_MULTIPLIER)
            );
        }
    }
}

