package csl.database;

import csl.database.model.Food;
import csl.database.model.FoodAlias;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static csl.database.FoodAliasRepository.*;


@Repository
public class FoodAliasRepository {
    public static final String TABLE_NAME = "food_alias";

    public static final String COL_ID = "id";
    public static final String COL_FOOD_ID = "food_id";
    public static final String COL_ALIASNAME = "aliasname";
    public static final String COL_DEFAULT_AMOUNT = "amount";
    public static final String COL_DEFAULT_AMOUNT_UNIT = "unitname";

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_FOOD_ID + " INT(6) NOT NULL, " +
                    COL_ALIASNAME + " TEXT NOT NULL, " +
                    COL_DEFAULT_AMOUNT + " DEC(5,2) NOT NULL," +
                    COL_DEFAULT_AMOUNT_UNIT + " TEXT NOT NULL," +
                    "FOREIGN KEY ("+COL_FOOD_ID+") REFERENCES "+FoodRepository.TABLE_NAME+"("+FoodRepository.COL_ID+")" +
                    ")";

    public static final String TABLE_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SELECT_SQL = "select * from "+ TABLE_NAME;
    private static final String INSERT_SQL = "insert into "+TABLE_NAME+"( food_Id,aliasname,amount,unitname) values(:foodId,:aliasname,:amount,:unitname)";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

    public FoodAliasRepository() {


    }

    public List<FoodAlias> getAllFoodAliasses() {
        return template.query(SELECT_SQL, new FoodAliasWrapper());
    }

    public int addFoodAlias(Food food, FoodAlias foodAlias) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("foodId",food.getId())
                .addValue("aliasname", foodAlias.getAliasname())
                .addValue("amount", foodAlias.getAmountNumber())
                .addValue("unitname", foodAlias.getAmountUnit());
        return template.update(INSERT_SQL, params);
    }

    public List<FoodAlias> getFoodAlias(String name) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("aliasname", name);
        String myFoodAlias = SELECT_SQL + " WHERE  " + COL_ALIASNAME + "= :aliasname";
        List<FoodAlias> queryResults = template.query(myFoodAlias, params, new FoodWrapper());
        return queryResults;
    }
}

class FoodAliasWrapper implements RowMapper {

    @Override
    public Object mapRow(ResultSet rs, int i) throws SQLException {
        return new FoodAlias(rs.getString(COL_ALIASNAME),
                rs.getDouble(COL_DEFAULT_AMOUNT),
                rs.getString(COL_DEFAULT_AMOUNT_UNIT)
                );
    }
}

