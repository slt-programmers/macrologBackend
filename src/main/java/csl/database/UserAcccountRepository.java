package csl.database;

import csl.database.model.UserAccount;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UserAcccountRepository {
    public static final String TABLE_NAME = "useraccounts";

    public static final String COL_ID = "id";
    public static final String TABLE_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_RESET_PASSWORD = "resetpassword";
    private static final String COL_RESET_DATE = "resetdate";
    private static final String COL_EMAIL = "email";

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INT(6) PRIMARY KEY AUTO_INCREMENT, " +
                    COL_USERNAME + " VARCHAR(30) UNIQUE NOT NULL, " +
                    COL_PASSWORD + " TEXT NOT NULL, " +
                    COL_RESET_PASSWORD + " TEXT, " +
                    COL_RESET_DATE + " DATETIME, " +
                    COL_EMAIL + " TEXT(100) NOT NULL)";
    private static final String SELECT_SQL = "select * from " + TABLE_NAME;
    private static final String INSERT_SQL = "insert into " + TABLE_NAME + "(" +
            "username, password, email) values(:username, :password, :email)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + " SET password = :password, resetpassword =:resetpassword, resetdate =:resetdate  where username = :username";

    private NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(new JdbcTemplate(DatabaseHelper.getInstance()));
    private static final Logger LOGGER = LoggerFactory.getLogger(UserAcccountRepository.class);

    public UserAcccountRepository() {
    }

    public int updatePassword(String username, String password,String resetpassword, LocalDateTime resetdate) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("username", username)
                .addValue("password", password)
                .addValue("resetpassword", resetpassword)
                .addValue("resetdate", resetdate==null?null:Timestamp.valueOf(resetdate));
        return template.update(UPDATE_SQL, params);
    }

    public int insertUser(String username, String password, String email) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", null)
                .addValue("username", username)
                .addValue("password", password)
                .addValue("email", email);
        return template.update(INSERT_SQL, params);
    }

    public UserAccount getUser(String username) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("username", username);
        String users = SELECT_SQL + " WHERE  " + COL_USERNAME + "= :username";
        List<UserAccount> queryResults = template.query(users, params, new UserWrapper<UserAccount>());
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public UserAccount getUserByEmail(String email) {
        if (email != null) {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("email", email.toUpperCase());
            String users = SELECT_SQL + " WHERE  UPPER(" + COL_EMAIL + ") = :email";
            List<UserAccount> queryResults = template.query(users, params, new UserWrapper<UserAccount>());
            return queryResults.isEmpty() ? null : queryResults.get(0);
        } else {
            return null;
        }
    }

    public UserAccount getUserById(Integer id) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);
        String users = SELECT_SQL + " WHERE " + COL_ID + " = :id";
        List<UserAccount> queryResults = template.query(users, params, new UserWrapper<UserAccount>());
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    class UserWrapper<T> implements RowMapper<UserAccount> {
        @Override
        public UserAccount mapRow(ResultSet rs, int i) throws SQLException {
            Timestamp ts = rs.getTimestamp(COL_RESET_DATE);
            return new UserAccount(rs.getLong(COL_ID),
                    rs.getString(COL_USERNAME),
                    rs.getString(COL_PASSWORD),
                    rs.getString(COL_EMAIL),
                    rs.getString(COL_RESET_PASSWORD),
                    ts==null?null:ts.toLocalDateTime()

            );
        }
    }
}

