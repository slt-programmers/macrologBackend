package slt.database;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import slt.database.model.Setting;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

@Repository
@Slf4j
public class SettingsRepository {
    public static final String TABLE_NAME = "settings";

    private static final String COL_ID = "id";
    private static final String COL_SETTING = "setting";
    private static final String COL_VALUE = "value";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_DATE = "date";


    private static final String SELECT_SQL = "SELECT * FROM " + TABLE_NAME;
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + "(user_id, setting, value, date) VALUES(:userId, :setting, :value, :date)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + " SET value = :value WHERE user_id = :userId AND setting = :setting AND date = :date";
    private static final String DELETE_ALL_SQL = "DELETE FROM " + TABLE_NAME + " WHERE user_id = :userId";

    @Autowired
    DatabaseHelper databaseHelper;

    @PostConstruct
    private void initTemplate() {
        template = new NamedParameterJdbcTemplate(new JdbcTemplate(databaseHelper));
    }

    private NamedParameterJdbcTemplate template;
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsRepository.class);

    public SettingsRepository() {
    }

    public int putSetting(Integer userId, String setting, String value) {
        Setting currentSetting = getLatestSetting(userId, setting);
        if (currentSetting == null) { // geen records
            LOGGER.debug("Insert");
            return insertSetting(userId, setting, value, null);
        } else {
            LOGGER.debug("Update");
            boolean settingFromToday = currentSetting.getDay().toLocalDate().equals(LocalDate.now());
            if (settingFromToday) {
                return updateSetting(userId, setting, value, Date.valueOf(LocalDate.now()));
            } else {
                return insertSetting(userId, setting, value, Date.valueOf(LocalDate.now()));

            }
        }
    }

    public int putSetting(Integer userId, String setting, String value, Date date) {
        Setting currentSetting = getValidSetting(userId, setting, date);
        if (currentSetting == null) { // geen records
            LOGGER.debug("Insert");
            return insertSetting(userId, setting, value, date);
        } else {
            LOGGER.debug("Update");
            boolean settingSameDay = currentSetting.getDay().toLocalDate().equals(date.toLocalDate());
            if (settingSameDay) {
                return updateSetting(userId, setting, value, date);
            } else {
                return insertSetting(userId, setting, value, date);

            }
        }
    }

    private int updateSetting(Integer userId, String setting, String value, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", null)
                .addValue("setting", setting)
                .addValue("value", value)
                .addValue("date", sdf.format(date));
        return template.update(UPDATE_SQL, params);
    }

    public int insertSetting(Integer userId, String setting, String value, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("id", null)
                .addValue("setting", setting)
                .addValue("value", value)
                .addValue("date", date == null ? sdf.format(LocalDate.now()) : sdf.format(date));
        return template.update(INSERT_SQL, params);
    }

    public int deleteAllForUser(Integer userId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        return template.update(DELETE_ALL_SQL, params);
    }

    public Setting getLatestSetting(Integer userId, String setting) {

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("setting", setting);
        String settings = SELECT_SQL + " WHERE  " + COL_SETTING + "= :setting AND " + COL_USER_ID + " = :userId order by date desc";
        List<Setting> queryResults = template.query(settings, params, new SettingsWrapper<Setting>());
        log.debug("Number of hits for {} :{}", setting, queryResults.size());
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public Setting getValidSetting(Integer userId, String setting, Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("setting", setting)
                .addValue("date", sdf.format(date));
        String settings = SELECT_SQL + " WHERE  " + COL_SETTING + "= :setting AND " + COL_USER_ID + " = :userId AND " + COL_DATE + " <= :date order by date desc";
        List<Setting> queryResults = template.query(settings, params, new SettingsWrapper<Setting>());
        log.debug("Number of hits for {} :{}", setting, queryResults.size());
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public List<Setting> getAllSettings(Integer userId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        return template.query(SELECT_SQL + " WHERE " + COL_USER_ID + " = :userId order by " + COL_DATE + " DESC", params, new SettingsWrapper<Setting>());
    }

    class SettingsWrapper<T> implements RowMapper<Setting> {

        @Override
        public Setting mapRow(ResultSet rs, int i) throws SQLException {
            return new Setting(rs.getLong(COL_ID),
                    rs.getString(COL_SETTING),
                    rs.getString(COL_VALUE),
                    rs.getDate(COL_DATE)
            );
        }
    }
}

