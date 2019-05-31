package csl.database;

import csl.database.model.LogEntry;
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

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_USER_ID + " INT(6) NOT NULL, " +
                    COL_WEIGHT + " DEC(5,2) NOT NULL, " +
                    COL_DAY + " DATE NOT NULL," +
                    "FOREIGN KEY (" + COL_USER_ID + ") REFERENCES " + UserAcccountRepository.TABLE_NAME + "(" + UserAcccountRepository.COL_ID + ")" +
                    ")";

    public static final String TABLE_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SELECT_SQL = "select * from " + TABLE_NAME;
    private static final String SELECT_ONE_SQL = "select * from " + TABLE_NAME + " where user_id = :userId and day = :day";
    private static final String INSERT_SQL = "insert into " + TABLE_NAME + "( user_id, weight, day) values(:userId,:weight,:day)";
    private static final String UPDATE_SQL = "update " + TABLE_NAME + " set weight = :weight,day = :day where Id = :id and user_id=:userId";
    private static final String DELETE_SQL = "delete from " + TABLE_NAME + " where id = :id AND user_id= :userId";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

    public int insertWeight(Integer userId, Weight entry) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("userId", userId)
                .addValue("weight", entry.getWeight())
                .addValue("day", entry.getDay());
        return template.update(INSERT_SQL, params);
    }

    public int updateWeight(Integer userId, Weight entry) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", entry.getId())
                .addValue("userId", userId)
                .addValue("weight", entry.getWeight())
                .addValue("day", entry.getDay());
        return template.update(UPDATE_SQL, params);
    }

    public int deleteWeight(Integer userId, Long entry) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", entry);
        return template.update(DELETE_SQL, params);
    }

    public List<Weight> getWeightEntryForDay(Integer userId, Date day) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("day", day);
        return template.query(SELECT_ONE_SQL, params, new WeightWrapper());
    }

        public List<Weight> getAllWeightEntries (Integer userId){
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("userId", userId);
            return template.query(SELECT_SQL + " WHERE user_id=:userId", params, new WeightWrapper());
        }

//    public List<LogEntry> getAllLogEntries(Integer userId, java.util.Date date) {
//        LOGGER.debug("Getting entries for " + date);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        SqlParameterSource params = new MapSqlParameterSource()
//                .addValue("userId", userId)
//                .addValue("date", sdf.format(date));
//        String myLogs = SELECT_SQL + " WHERE  " + COL_DAY + "= :date AND user_id=:userId";
//        return template.query(myLogs, params, new LogEntryWrapper());
//    }
//
//    public List<LogEntry> getAllLogEntries(Integer userId, java.util.Date begin, java.util.Date end) {
//        LOGGER.debug("Getting entries for period " + begin + " - " + end);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        SqlParameterSource params = new MapSqlParameterSource()
//                .addValue("userId", userId)
//                .addValue("dateBegin", sdf.format(begin))
//                .addValue("dateEnd", sdf.format(end));
//        String myLogs = SELECT_SQL + " WHERE  " + COL_DAY + ">= :dateBegin AND " + COL_DAY + "<= :dateEnd AND user_id=:userId";
//        LOGGER.debug(myLogs);
//        LOGGER.debug("between " + sdf.format(begin) + " and " + sdf.format(end));
//        return template.query(myLogs, params, new LogEntryWrapper());
//    }

        class WeightWrapper implements RowMapper {
            @Override
            public Object mapRow(ResultSet rs, int i) throws SQLException {
                Date ts = rs.getDate(COL_DAY);
                return new Weight(rs.getLong(COL_ID),
                        rs.getDouble(COL_WEIGHT),
                        ts);

            }
        }

}