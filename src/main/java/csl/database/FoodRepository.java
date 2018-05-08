package csl.database;

import csl.database.model.Food;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Carmen on 18-3-2018.
 */
@Repository
public class FoodRepository {

    private static final String SELECT_SQL = "select * from food";
    private static final String INSERT_SQL = "insert into food(id, name) values(:id, :name)";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

    public List<Food> getAllFood() {
        return template.query(SELECT_SQL, new FoodWrapper());
    }

    public int insertFood(Food food) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", 1)
                .addValue("name", food.getName())
                .addValue("unit", food.getUnit())
                .addValue("unitname", food.getUnitName())
                .addValue("optionalgrams", food.getOptionalGrams())
                .addValue("protein", food.getProtein())
                .addValue("fat", food.getFat())
                .addValue("carbs", food.getCarbs());
        return template.update(INSERT_SQL, params);
    }

}

class FoodWrapper implements RowMapper {

    @Override
    public Object mapRow(ResultSet rs, int i) throws SQLException {
        return new Food(rs.getString("name"),
                rs.getString("unit"),
                rs.getString("unitname"),
                rs.getInt("optionalgrams"),
                rs.getInt("protein"),
                rs.getInt("fat"),
                rs.getInt("carbs")
                );
    }

}