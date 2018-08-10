package csl.database;

import csl.database.model.Setting;
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
import java.util.List;

@Repository
public class SettingsRepository {
    public static final String TABLE_NAME = "settings";

    private static final String COL_ID = "id";
    public static final String TABLE_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final String COL_SETTING = "setting";
    private static final String COL_VALUE = "value";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_DATE = "date";
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_USER_ID + " INT(6) NOT NULL, " +
                    COL_SETTING + " TEXT NOT NULL, " +
                    COL_VALUE + " TEXT," +
                    "FOREIGN KEY (" + COL_USER_ID + ") REFERENCES " + UserAcccountRepository.TABLE_NAME + "(" + UserAcccountRepository.COL_ID + ")," +
                    COL_DATE + " DATE" +
                    ")";
    private static final String SELECT_SQL = "select * from settings";
    private static final String INSERT_SQL = "insert into settings" +
            "(user_id,setting, value) values(:userId,:setting, :value)";
    private static final String UPDATE_SQL = "UPDATE settings SET value = :value where user_id = :userId AND setting = :setting";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsRepository.class);

    public SettingsRepository() {
    }

    public int putSetting(Integer userId,String setting, String value) {
        if (getSetting(userId,setting) == null) {
            LOGGER.debug("Insert");
            return insertSetting(userId,setting, value);
        } else {
            LOGGER.debug("Update");
            return updateSetting(userId,setting, value);
        }
    }

    private int updateSetting(Integer userId, String setting, String value) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", null)
                .addValue("setting", setting)
                .addValue("value", value);
        return template.update(UPDATE_SQL, params);
    }

    public int insertSetting(Integer userId, String setting, String value) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", null)
                .addValue("setting", setting)
                .addValue("value", value);
        return template.update(INSERT_SQL, params);
    }

    public int insertSetting(Integer userId, String setting, String value, Date date) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", null)
                .addValue("setting", setting)
                .addValue("value", value)
                .addValue("date", date);
        return template.update(INSERT_SQL, params);
    }

    public String getSetting(Integer userId, String setting) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("setting", setting);
        String settings = SELECT_SQL + " WHERE  " + COL_SETTING + "= :setting AND " + COL_USER_ID + "=:userId";
        List<Setting> queryResults = template.query(settings, params, new SettingsWrapper<Setting>());
        return queryResults.isEmpty() ? null : queryResults.get(0).getValue();
    }

    private List<Setting> getAllSettings() {
        return template.query(SELECT_SQL, new SettingsWrapper<Setting>());
    }
    public List<Setting> getAllSettings(Integer userId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        return template.query(SELECT_SQL + " WHERE " + COL_USER_ID + "=:userId", params,new SettingsWrapper<Setting>());
    }

    class SettingsWrapper<T> implements RowMapper<Setting> {

        @Override
        public Setting mapRow(ResultSet rs, int i) throws SQLException {
            return new Setting(rs.getLong(COL_ID),
                    rs.getString(COL_SETTING),
                    rs.getString(COL_VALUE)
            );
        }
    }
}

