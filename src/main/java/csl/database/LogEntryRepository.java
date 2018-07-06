package csl.database;

import csl.database.model.LogEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static csl.database.LogEntryRepository.*;

/**
 * Created by Carmen on 18-3-2018.
 */
@Repository
public class LogEntryRepository {
    public static final String TABLE_NAME = "logentry";

    public static final String COL_ID = "id";
    public static final String COL_FOOD_ID = "food_id";
    public static final String COL_ALIAS_ID = "alias_id";
    public static final String COL_MULTIPLIER = "multiplier";
    public static final String COL_DAY = "day";
    public static final String COL_MEAL = "meal";

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_FOOD_ID + " INT(6)NOT NULL, " +
                    COL_ALIAS_ID + " INT(6)  NOT NULL, " +
                    COL_MULTIPLIER + " DEC(5,2) NOT NULL, " +
                    COL_DAY + " DATE NOT NULL," +
                    COL_MEAL + " TEXT" +
                    ")";

    public static final String TABLE_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public int insertLogEntry(LogEntry entry) {

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("foodId", entry.getFoodId())
                .addValue("aliasId", entry.getAliasIdUsed())
                .addValue("multiplier", entry.getMultiplier())
                .addValue("day", entry.getDay())
                .addValue("meal", entry.getMeal());
        return template.update(INSERT_SQL, params);
    }

    private static final String SELECT_SQL = "select * from " + TABLE_NAME;
    private static final String INSERT_SQL = "insert into " + TABLE_NAME + "( food_Id,alias_Id,multiplier,day,meal) values(:foodId,:aliasId,:multiplier,:day,:meal)";
    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));


    public List<LogEntry> getAllLogEntries() {
        return template.query(SELECT_SQL, new LogEntryWrapper());
    }

}

class LogEntryWrapper implements RowMapper {


    @Override
    public Object mapRow(ResultSet rs, int i) throws SQLException {


        Date ts = rs.getDate(COL_DAY);
        return new LogEntry(rs.getLong(COL_ID),
                rs.getLong(COL_FOOD_ID),
                rs.getLong(COL_ALIAS_ID),
                rs.getDouble(COL_MULTIPLIER),
                ts,
                rs.getString(COL_MEAL));

    }
}