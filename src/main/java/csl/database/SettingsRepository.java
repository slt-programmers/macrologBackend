package csl.database;

import csl.database.model.Setting;
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
public class SettingsRepository {
    public static final String TABLE_NAME = "settings";

    public static final String COL_ID = "id";
    public static final String TABLE_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final String COL_SETTING = "setting";
    private static final String COL_VALUE = "value";
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_SETTING + " TEXT NOT NULL, " +
                    COL_VALUE + " TEXT)";
    private static final String SELECT_SQL = "select * from settings";
    private static final String INSERT_SQL = "insert into settings(" +
            "setting, value) values(:setting, :value)";
    private static final String UPDATE_SQL = "UPDATE settings SET value = :value where setting = :setting";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));

    public SettingsRepository() {
    }

    public int putSetting(String setting, String value) {
        if (getSetting(setting) == null) {
            System.out.println("Insert");
            return insertSetting(setting, value);
        } else {
            System.out.println("Update");
            return updateSetting(setting, value);
        }
    }

    private int updateSetting(String setting, String value) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("setting", setting)
                .addValue("value", value);
        return template.update(UPDATE_SQL, params);
    }

    public int insertSetting(String setting, String value) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("setting", setting)
                .addValue("value", value);
        return template.update(INSERT_SQL, params);
    }

    public String getSetting(String setting) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("setting", setting);
        String settings = SELECT_SQL + " WHERE  " + COL_SETTING + "= :setting";
        List<Setting> queryResults = template.query(settings, params, new SettingsWrapper<Setting>());
        return queryResults.isEmpty() ? null : queryResults.get(0).getValue();
    }

    public List<Setting> getAllSettings() {
        return template.query(SELECT_SQL, new SettingsWrapper<Setting>());
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

