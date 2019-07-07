package csl.database;

import csl.database.model.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UserAcccountRepository {
    public static final String TABLE_NAME = "useraccounts";

    static final String COL_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_RESET_PASSWORD = "reset_password";
    private static final String COL_RESET_DATE = "reset_date";
    private static final String COL_EMAIL = "email";

    private static final String SELECT_SQL = "SELECT * FROM " + TABLE_NAME;
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + "(username, password, email) VALUES(:username, :password, :email)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + " SET password = :password, reset_password = :reset_password, reset_date = :reset_date WHERE username = :username";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE id = :id";

    @Autowired
    DatabaseHelper databaseHelper;

    @PostConstruct
    private void initTemplate() {
        template = new NamedParameterJdbcTemplate(new JdbcTemplate(databaseHelper));
    }

    private NamedParameterJdbcTemplate template;

    public UserAcccountRepository() {
    }

    public int updatePassword(String username, String password, String resetPassword, LocalDateTime resetDate) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("username", username)
                .addValue("password", password)
                .addValue("reset_password", resetPassword)
                .addValue("reset_date", resetDate == null ? null : Timestamp.valueOf(resetDate));
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
            String users = SELECT_SQL + " WHERE UPPER(" + COL_EMAIL + ") = :email";
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

    public int deleteUser(Integer id) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);
        return template.update(DELETE_SQL, params);
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
                    ts == null ? null : ts.toLocalDateTime()
            );
        }
    }
}

