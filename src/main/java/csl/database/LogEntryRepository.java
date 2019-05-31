package csl.database;

import csl.database.model.LogEntry;
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
public class LogEntryRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryRepository.class);

    public static final String TABLE_NAME = "logentry";

    private static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_FOOD_ID = "food_id";
    private static final String COL_PORTION_ID = "portion_id";
    private static final String COL_MULTIPLIER = "multiplier";
    private static final String COL_DAY = "day";
    private static final String COL_MEAL = "meal";

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_USER_ID + " INT(6) NOT NULL, " +
                    COL_FOOD_ID + " INT(6) NOT NULL, " +
                    COL_PORTION_ID + " INT(6) NULL, " +
                    COL_MULTIPLIER + " DEC(5,2) NOT NULL, " +
                    COL_DAY + " DATE NOT NULL," +
                    COL_MEAL + " TEXT," +
                    "FOREIGN KEY (" + COL_FOOD_ID + ") REFERENCES " + FoodRepository.TABLE_NAME + "(" + FoodRepository.COL_ID + ")," +
                    "FOREIGN KEY (" + COL_PORTION_ID + ") REFERENCES " + PortionRepository.TABLE_NAME + "(" + PortionRepository.COL_ID + ")," +
                    "FOREIGN KEY (" + COL_USER_ID + ") REFERENCES " + UserAcccountRepository.TABLE_NAME + "(" + UserAcccountRepository.COL_ID + ")" +
                    ")";

    private static final String SELECT_SQL = "SELECT * FROM " + TABLE_NAME;
    private static final String SELECT_ONE_SQL = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = :userId AND food_id = :foodId AND day = :day AND meal = :meal";
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + "(user_id, food_Id, portion_Id, multiplier, day, meal) VALUES(:userId, :foodId, :portionId, :multiplier, :day, :meal)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + " SET food_id = :foodId, portion_Id = :portionId, multiplier = :multiplier, day = :day, meal = :meal WHERE Id = :id AND user_id = :userId";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE id = :id AND user_id = :userId";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

    public int insertLogEntry(Integer userId, LogEntry entry) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("userId", userId)
                .addValue("foodId", entry.getFoodId())
                .addValue("portionId", entry.getPortionId())
                .addValue("multiplier", entry.getMultiplier())
                .addValue("day", entry.getDay())
                .addValue("meal", entry.getMeal());
        return template.update(INSERT_SQL, params);
    }

    public int updateLogEntry(Integer userId, LogEntry entry) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", entry.getId())
                .addValue("userId", userId)
                .addValue("foodId", entry.getFoodId())
                .addValue("portionId", entry.getPortionId())
                .addValue("multiplier", entry.getMultiplier())
                .addValue("day", entry.getDay())
                .addValue("meal", entry.getMeal());
        return template.update(UPDATE_SQL, params);
    }

    public int deleteLogEntry(Integer userId, Long entry) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", entry);
        return template.update(DELETE_SQL, params);
    }

    public List<LogEntry> getLogEntry(Integer userId, Long foodId, Date day, String meal) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("foodId", foodId)
                .addValue("day", day)
                .addValue("meal", meal);
        return template.query(SELECT_ONE_SQL, params, new LogEntryWrapper());
    }

    public List<LogEntry> getAllLogEntries(Integer userId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        return template.query(SELECT_SQL + " WHERE " + COL_USER_ID + " = :userId", params, new LogEntryWrapper());
    }

    public List<LogEntry> getAllLogEntries(Integer userId, java.util.Date date) {
        LOGGER.debug("Getting entries for " + date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("date", sdf.format(date));
        String myLogs = SELECT_SQL + " WHERE  " + COL_DAY + "= :date AND " + COL_USER_ID + " = :userId";
        return template.query(myLogs, params, new LogEntryWrapper());
    }

    public List<LogEntry> getAllLogEntries(Integer userId, java.util.Date begin, java.util.Date end) {
        LOGGER.debug("Getting entries for period " + begin + " - " + end);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("dateBegin", sdf.format(begin))
                .addValue("dateEnd", sdf.format(end));
        String myLogs = SELECT_SQL + " WHERE  " + COL_DAY + ">= :dateBegin AND " + COL_DAY + "<= :dateEnd AND " + COL_USER_ID + " = :userId";
        LOGGER.debug(myLogs);
        LOGGER.debug("BETWEEN " + sdf.format(begin) + " AND " + sdf.format(end));
        return template.query(myLogs, params, new LogEntryWrapper());
    }

    class LogEntryWrapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            Date ts = rs.getDate(COL_DAY);
            return new LogEntry(rs.getLong(COL_ID),
                    rs.getLong(COL_FOOD_ID),
                    rs.getLong(COL_PORTION_ID),
                    rs.getDouble(COL_MULTIPLIER),
                    ts,
                    rs.getString(COL_MEAL));

        }
    }
}