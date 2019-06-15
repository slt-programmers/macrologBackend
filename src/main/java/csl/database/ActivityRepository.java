package csl.database;

import csl.database.model.LogActivity;
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
public class ActivityRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityRepository.class);

    public static final String TABLE_NAME = "activity";

    private static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_NAME = "name";
    private static final String COL_CALORIES = "calories";
    private static final String COL_DAY = "day";

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_USER_ID + " INT(6) NOT NULL, " +
                    COL_NAME + " TEXT NOT NULL, " +
                    COL_CALORIES + " INT(6) NOT NULL, " +
                    COL_DAY + " DATE NOT NULL," +
                    "FOREIGN KEY (" + COL_USER_ID + ") REFERENCES " + UserAcccountRepository.TABLE_NAME + "(" + UserAcccountRepository.COL_ID + ")" +
                    ")";

    private static final String SELECT_SQL = "SELECT * FROM " + TABLE_NAME;
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + "(user_id, name, calories, day) VALUES(:userId, :name, :calories, :day)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + " SET name = :name, calories = :calories, day = :day WHERE Id = :id AND user_id = :userId";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE id = :id AND user_id = :userId";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

    public int insertActivity(Integer userId, LogActivity entry) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("userId", userId)
                .addValue("name", entry.getName())
                .addValue("calories", entry.getCalories())
                .addValue("day", entry.getDay());
        return template.update(INSERT_SQL, params);
    }

    public int updateLogActivity(Integer userId, LogActivity entry) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", entry.getId())
                .addValue("userId", userId)
                .addValue("name", entry.getName())
                .addValue("calories", entry.getCalories())
                .addValue("day", entry.getDay());
        return template.update(UPDATE_SQL, params);
    }

    public int deleteLogActivity(Integer userId, Long entry) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", entry);
        return template.update(DELETE_SQL, params);
    }

    public List<LogActivity> getAllLogActivities(Integer userId) {
        LOGGER.debug("Getting entries for " + userId);
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        String myLogs = SELECT_SQL + " WHERE  " + COL_USER_ID + " = :userId";
        return template.query(myLogs, params, new LogActivityWrapper());
    }

    public List<LogActivity> getAllLogActivities(Integer userId, java.util.Date date) {
        LOGGER.debug("Getting entries for " + date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("date", sdf.format(date));
        String myLogs = SELECT_SQL + " WHERE  " + COL_DAY + "= :date AND " + COL_USER_ID + " = :userId";
        return template.query(myLogs, params, new LogActivityWrapper());
    }

    class LogActivityWrapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            Date ts = rs.getDate(COL_DAY);
            return new LogActivity(rs.getLong(COL_ID),
                    rs.getString(COL_NAME),
                    rs.getDouble(COL_CALORIES),
                    ts);

        }
    }
}