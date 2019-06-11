package csl.database;

import csl.database.model.Weight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

@Repository
public class WeightRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeightRepository.class);

    public static final String TABLE_NAME = "weight";

    private static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_WEIGHT = "weight";
    private static final String COL_DAY = "day";
    private static final String COL_REMARK = "remark";

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_USER_ID + " INT(6) NOT NULL, " +
                    COL_WEIGHT + " DEC(5,2) NOT NULL, " +
                    COL_REMARK + " TEXT, " +
                    COL_DAY + " DATE NOT NULL," +
                    "FOREIGN KEY (" + COL_USER_ID + ") REFERENCES " + UserAcccountRepository.TABLE_NAME + "(" + UserAcccountRepository.COL_ID + ")" +
                    ")";

    public static final String TABLE_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SELECT_SQL = "SELECT * FROM " + TABLE_NAME;
    private static final String SELECT_ONE_SQL = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = :userId AND day = :day";
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + "(user_id, weight, day, remark) VALUES(:userId, :weight, :day, :remark)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + " SET weight = :weight, day = :day, remark = :remark WHERE Id = :id AND user_id = :userId";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE id = :id AND user_id= :userId";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

    public int insertWeight(Integer userId, Weight entry) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("userId", userId)
                .addValue("weight", entry.getWeight())
                .addValue("day", sdf.format(entry.getDay()))
                .addValue("remark", entry.getRemark());
        return template.update(INSERT_SQL, params);
    }

    public int updateWeight(Integer userId, Weight entry) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", entry.getId())
                .addValue("userId", userId)
                .addValue("weight", entry.getWeight())
                .addValue("day", sdf.format(entry.getDay()))
                .addValue("remark", entry.getRemark());
        return template.update(UPDATE_SQL, params);
    }

    public int deleteWeight(Integer userId, Long entry) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", entry);
        return template.update(DELETE_SQL, params);
    }

    public List<Weight> getWeightEntryForDay(Integer userId, Date day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("day", sdf.format(day));
        return template.query(SELECT_ONE_SQL, params, new WeightWrapper());
    }

    public List<Weight> getAllWeightEntries(Integer userId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        return template.query(SELECT_SQL + " WHERE user_id = :userId", params, new WeightWrapper());
    }

    class WeightWrapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            return new Weight(rs.getLong(COL_ID),
                    rs.getDouble(COL_WEIGHT),
                    rs.getDate(COL_DAY),
                    rs.getString(COL_REMARK));

        }
    }
}