package slt.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import slt.database.model.LogActivity;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@Slf4j
public class ActivityRepository {

    public static final String TABLE_NAME = "activity";

    private static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_NAME = "name";
    private static final String COL_CALORIES = "calories";
    private static final String COL_DAY = "day";

    private static final String SELECT_SQL = "SELECT * FROM " + TABLE_NAME;
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + "(user_id, name, calories, day) VALUES(:userId, :name, :calories, :day)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + " SET name = :name, calories = :calories, day = :day WHERE Id = :id AND user_id = :userId";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE id = :id AND user_id = :userId";
    private static final String DELETE_ALL_SQL = "DELETE FROM " + TABLE_NAME + " WHERE user_id = :userId";

    DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    DatabaseHelper databaseHelper;

    @PostConstruct
    private void initTemplate() {
        template = new NamedParameterJdbcTemplate(new JdbcTemplate(databaseHelper));
    }

    private NamedParameterJdbcTemplate template;

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

    public int deleteAllForUser(Integer userId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        return template.update(DELETE_ALL_SQL, params);
    }

    public List<LogActivity> getAllLogActivities(Integer userId) {
        log.debug("Getting entries for " + userId);
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        String myLogs = SELECT_SQL + " WHERE  " + COL_USER_ID + " = :userId";
        return template.query(myLogs, params, new LogActivityWrapper());
    }

    public List<LogActivity> getAllLogActivities(Integer userId, LocalDate date) {
        log.debug("Getting entries for " + date);
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("date",date.format(dbFormatter));
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